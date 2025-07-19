package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;

public class EditorWebApplication implements AutoCloseable {

    private final int port;
    private final WebDriver driver;
    private Process process;

    public static EditorWebApplication launch() {
        final Path executable = Path.of("./target/swe");

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

    public EditorWebApplication addEntry(final String type) {
        final Select select = new Select(driver.findElement(By.name("type")));
        select.selectByVisibleText(type);

        driver.findElement(By.name("submit")).click();
        return this;
    }

    public EditorWebApplication assertLastEntryOfType(final String expected) {
        final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(textToBePresentInElementLocated(By.xpath("//ul[@id='entries']/li[last()]"), expected));
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
                } catch (InterruptedException ie) {
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
}
