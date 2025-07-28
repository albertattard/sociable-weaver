package demo;

import demo.rest.EntryType;
import demo.rest.HeadingLevel;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class EditorWebApplication implements AutoCloseable {

    private final int port;
    private final WebDriver driver;
    private Process process;

    public static EditorWebApplication launch() {
        final Path executable = Path.of("./target/swe");
        if (!Files.isExecutable(executable)) {
            throw new RuntimeException("The native executable '" + executable + "' is missing. Please make sure to build the native executable is built before running the functional tests.");
        }

        try {
            final int port = findFreePort();

            final File log = File.createTempFile("swe-", ".log");
            log.deleteOnExit();

            final ProcessBuilder builder = new ProcessBuilder(executable.toString(), "--server.port=" + port);
            builder.redirectErrorStream(true);
            builder.redirectOutput(log);
            final Process process = builder.start();
            waitForPort(port);

            final ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");              // Run in headless mode (no UI)
            options.addArguments("--disable-gpu");           // Recommended for Windows
            options.addArguments("--window-size=1920,1080"); // Set a standard size
            final ChromeDriver driver = new ChromeDriver(options);

            return new EditorWebApplication(port, driver, process);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to launch the application", e);
        }
    }

    private EditorWebApplication(final int port, final WebDriver driver, Process process) {
        this.port = port;
        this.driver = driver;
        this.process = process;
    }

    public EditorWebApplication openEditorPage() {
        driver.get("http://localhost:%d/".formatted(port));
        return this;
    }

    public EditorWebApplication addEntry(final EntryType type) {
        final Select select = new Select(driver.findElement(By.name("type")));
        select.selectByVisibleText(type.name());

        driver.findElement(By.name("submit")).click();
        return this;
    }

    private EditorWebApplication selectOptionElementAtIndex(final int index, final String cssSelector, final String option) {
        new Select(find(index, cssSelector)).selectByVisibleText(option);
        return this;
    }

    private EditorWebApplication clickOnElementAtIndex(final int index, final String cssSelector) {
        find(index, cssSelector).click();
        return this;
    }

    private EditorWebApplication setInputValueAtIndex(final int index, final String cssSelector, final String value) {
        final WebElement element = find(index, cssSelector);
        element.clear();
        element.sendKeys(value);
        return this;
    }

    private EditorWebApplication waitForElementToBeVisible(final int index, final String cssSelector) {
        return waitForElementToBeVisible(index, cssSelector, Duration.ofSeconds(1));
    }

    private EditorWebApplication waitForElementToBeVisible(final int index, final String cssSelector, final Duration waitFor) {
        new WebDriverWait(driver, waitFor)
                .until(visibilityOfElementLocated(by(index, cssSelector)));
        return this;
    }

    private EditorWebApplication assertElementAtIndexContains(final int index, final String cssSelector, final String expectedContent) {
        return assertContainsText("ul#entries > li:nth-of-type(" + (index + 1) + ") " + cssSelector, expectedContent);
    }

    private EditorWebApplication assertElementAtIndexVisible(final int index, final String cssSelector) {
        final WebElement element = find(index, cssSelector);
        if (!element.isDisplayed()) {
            throw new AssertionError("Element '" + cssSelector + "' at index " + index + " is not visible");
        }
        return this;
    }

    private EditorWebApplication assertContainsText(final String cssSelector, final String expectedContent) {
        final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(textToBePresentInElementLocated(By.cssSelector(cssSelector), expectedContent));
        return this;
    }

    private WebElement find(final int index, final String cssSelector) {
        return driver.findElement(by(index, cssSelector));
    }

    private static By by(final int index, final String cssSelector) {
        return By.cssSelector("ul#entries > li:nth-of-type(" + (index + 1) + ") " + cssSelector);
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to find a free port");
        }
    }

    private static void waitForPort(final int port) {
        for (int i = 0; i < 50; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", port), 200);
                return;
            } catch (final IOException e) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for the port to respond", ie);
                }
            }
        }

        throw new RuntimeException("Failed to start the native application");
    }

    @Override
    public void close() {
        driver.quit();

        if (process != null) {
            try {
                process.destroy();
                try {
                    process.waitFor();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for the process to exit", e);
                }
            } finally {
                this.process = null;
            }
        }
    }

    public Row row(final int index) {
        return new Row(index, this);
    }

    public record Row(int index, EditorWebApplication application) {

        public EditForm addAfter(final EntryType type) {
            application.selectOptionElementAtIndex(index, "> select", type.name());
            return new EditForm(next());
        }

        public EditForm clickEditButton() {
            application.clickOnElementAtIndex(index, "> button[name=edit]");
            return new EditForm(this);
        }

        public EditorWebApplication clickDeleteButton() {
            application.clickOnElementAtIndex(index, "> button[name=delete]");
            return application;
        }

        public EditorWebApplication clickUndoButton() {
            application.clickOnElementAtIndex(index, "> button[name=undo]");
            return application;
        }

        private Row waitForElementToBeVisible(final String cssSelector) {
            application.waitForElementToBeVisible(index, cssSelector);
            return this;
        }

        public Row assertTitleContains(final String expected) {
            application.assertElementAtIndexContains(index, "> h2", expected);
            return this;
        }

        private Row setInputValue(final String cssSelector, final String value) {
            application.setInputValueAtIndex(index, cssSelector, value);
            return this;
        }

        private Row clickOnElement(final String cssSelector) {
            application.clickOnElementAtIndex(index, cssSelector);
            return this;
        }

        private Row assertElementContains(final String cssSelector, final String expected) {
            application.assertElementAtIndexContains(index, cssSelector, expected);
            return this;
        }

        public Row assertContains(final String expected) {
            application.assertContainsText("ul#entries > li:nth-of-type(" + (index + 1) + ")", expected);
            return this;
        }

        private Row assertElementVisible(final String cssSelector) {
            application.assertElementAtIndexVisible(index, cssSelector);
            return this;
        }

        private Row selectOptionElement(final String cssSelector, final String value) {
            application.selectOptionElementAtIndex(index, cssSelector, value);
            return null;
        }

        private Row next() {
            return new Row(index + 1, application);
        }
    }

    public record EditForm(Row row) {

        public EditForm waitForEditFormToBeVisible() {
            row.waitForElementToBeVisible("> form[name=edit]");
            return this;
        }

        public EditForm setTitle(final String title) {
            row.setInputValue("> form[name=edit] input[name=title]", title);
            return this;
        }

        public EditForm assertTitleInputVisible() {
            row.assertElementVisible("> form[name=edit] input[name=title]");
            return this;
        }

        public EditForm assertTitleContains(final String expected) {
            row.assertElementContains("> form[name=edit] input[name=title]", expected);
            return this;
        }

        public EditForm assertLevelSelectVisible() {
            row.assertElementVisible("> form[name=edit] select[name=level]");
            return this;
        }

        public EditForm selectLevel(final HeadingLevel value) {
            row.selectOptionElement("> form[name=edit] select[name=level]", value.name());
            return this;
        }

        public EditForm assertUpdateButtonVisible() {
            row.assertElementVisible("> form[name=edit] > button[name=update]");
            return this;
        }

        public Row clickUpdateButton() {
            return row.clickOnElement("> form[name=edit] > button[name=update]");
        }

        public EditForm assertCancelButtonVisible() {
            row.assertElementVisible("> form[name=edit] > button[name=cancel]");
            return this;
        }

        public Row clickCancelButton() {
            return row.clickOnElement("> form[name=edit] > button[name=cancel]");
        }

        /* TODO: Assert that the other fields are not visible */
        public EditForm assertHeadingFieldsVisible() {
            return assertTitleInputVisible()
                    .assertLevelSelectVisible()
                    .assertUpdateButtonVisible()
                    .assertCancelButtonVisible();
        }
    }
}
