package demo;

import demo.rest.EntryType;
import demo.rest.HeadingLevel;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
import java.util.function.Function;
import java.util.function.Supplier;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;

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

    public record Row(int index, EditorWebApplication application) implements WebContainer {

        private static final By EDIT_BUTTON = By.cssSelector("button[name=edit]");
        private static final By DELETE_BUTTON = By.cssSelector("button[name=delete]");
        private static final By UNDO_BUTTON = By.cssSelector("button[name=undo]");
        private static final By SELECT_NEW_TYPE = By.cssSelector("select[name=type]");

        public EditForm addAfter(final EntryType type) {
            selectOption(SELECT_NEW_TYPE, type.name());
            return new EditForm(next());
        }

        public EditForm clickEditButton() {
            clickOn(EDIT_BUTTON);
            return new EditForm(this);
        }

        public Row clickDeleteButton() {
            clickOn(DELETE_BUTTON);
            return this;
        }

        public Row clickUndoButton() {
            clickOn(UNDO_BUTTON);
            return this;
        }

        public Row waitForHeadingToBeVisible(final HeadingLevel level) {
            waitForElementToBeVisible(() -> findElement(headingLocator(level)));
            return this;
        }

        public Row assertTitleContains(final HeadingLevel level, final String expected) {
            assertElementTextContains(findElement(headingLocator(level)), expected);
            return this;
        }

        public Row assertRowTextContains(final String expected) {
            assertElementTextContains(element(), expected);
            return this;
        }

        private static By headingLocator(final HeadingLevel level) {
            return By.cssSelector(level.name().toLowerCase() + "[name=title]");
        }

        private Row next() {
            return row(index + 1);
        }

        public Row row(final int index) {
            return application.row(index);
        }

        public WebElement element() {
            final By cssSelector = By.cssSelector("ul#entries > li:nth-of-type(" + (index + 1) + ")");
            return driver().findElement(cssSelector);
        }

        public WebDriver driver() {
            return application.driver;
        }
    }

    public record EditForm(Row row) implements WebContainer {

        private static final By FORM = By.cssSelector("form[name=edit]");
        private static final By UPDATE_BUTTON = By.cssSelector("button[name=update]");
        private static final By CANCEL_BUTTON = By.cssSelector("button[name=cancel]");
        private static final By TITLE_INPUT = By.cssSelector("input[name=title]");
        private static final By LEVEL_SELECT = By.cssSelector("select[name=level]");

        public EditForm waitForEditFormToBeVisible() {
            waitForElementToBeVisible(this::element);
            return this;
        }

        public EditForm setTitle(final String title) {
            setInputValue(TITLE_INPUT, title);
            return this;
        }

        public EditForm assertTitleInputVisible() {
            assertVisible(TITLE_INPUT);
            return this;
        }

        public EditForm assertTitleContains(final String expected) {
            assertThat(textToBePresentInElementValue(findElement(TITLE_INPUT), expected));
            return this;
        }

        public EditForm assertLevelSelectVisible() {
            assertVisible(LEVEL_SELECT);
            return this;
        }

        public EditForm selectLevel(final HeadingLevel value) {
            selectOption(LEVEL_SELECT, value.name());
            return this;
        }

        public EditForm assertUpdateButtonVisible() {
            assertVisible(UPDATE_BUTTON);
            return this;
        }

        public Row clickUpdateButton() {
            clickOn(UPDATE_BUTTON);
            return row;
        }

        public EditForm assertCancelButtonVisible() {
            assertVisible(CANCEL_BUTTON);
            return this;
        }

        public Row clickCancelButton() {
            clickOn(CANCEL_BUTTON);
            return row;
        }

        /* TODO: Assert that the other fields are not visible */
        public EditForm assertHeadingFieldsVisible() {
            return assertTitleInputVisible()
                    .assertLevelSelectVisible()
                    .assertUpdateButtonVisible()
                    .assertCancelButtonVisible();
        }

        public WebElement element() {
            return row.findElement(FORM);
        }

        public WebDriver driver() {
            return row.driver();
        }
    }

    public interface WebContainer {

        default void waitForElementToBeVisible(final Supplier<WebElement> supplier) {
            new WebDriverWait(driver(), Duration.ofSeconds(1))
                    .until((ExpectedCondition<WebElement>) driver -> {
                                try {
                                    final WebElement element = supplier.get();
                                    return element != null && element.isDisplayed()
                                            ? element
                                            : null;
                                } catch (final NoSuchElementException | StaleElementReferenceException e) {
                                    return null;
                                }
                            }
                    );
        }

        default void setInputValue(final By by, final String value) {
            final WebElement element = findElement(by);
            element.clear();
            element.sendKeys(value);
        }

        default void clickOn(final By by) {
            findElement(by).click();
        }

        default void selectOption(final By by, final String text) {
            new Select(findElement(by)).selectByVisibleText(text);
        }

        default void assertVisible(final By by) {
            assertVisible(by, true);
        }

        default void assertVisible(final By by, final boolean visible) {
            if (visible != findElement(by).isDisplayed()) {
                throw new AssertionError("Element '" + by + "' is not visible");
            }
        }

        default void assertElementTextContains(final WebElement element, final String expected) {
            assertThat(textToBePresentInElement(element, expected));
        }

        default void assertThat(final Function<WebDriver, Boolean> isTrue) {
            new WebDriverWait(driver(), Duration.ofSeconds(1)).until(isTrue);
        }

        default WebElement findElement(final By by) {
            return element().findElement(by);
        }

        WebElement element();

        WebDriver driver();
    }
}
