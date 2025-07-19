package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;

public class EditorWebApplication implements AutoCloseable {

    private final int port;
    private final WebDriver driver;

    public static EditorWebApplication create(final int port) {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");              // Run in headless mode (no UI)
        options.addArguments("--disable-gpu");           // Recommended for Windows
        options.addArguments("--window-size=1920,1080"); // Set a standard size
        return new EditorWebApplication(port, new ChromeDriver(options));
    }

    private EditorWebApplication(final int port, final WebDriver driver) {
        this.port = port;
        this.driver = driver;
    }

    public EditorWebApplication openEditorPage() {
        driver.get("http://localhost:%d/".formatted(port));
        return this;
    }

    public EditorWebApplication addItem(final String name) {
        final WebElement input = driver.findElement(By.name("name"));
        input.sendKeys(name);
        input.submit();
        return this;
    }

    public EditorWebApplication assertLastItemIs(final String expected) {
        final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(textToBePresentInElementLocated(By.xpath("//ul[@id='entries']/li[last()]"), expected));
        return this;
    }

    @Override
    public void close() {
        driver.quit();
    }
}
