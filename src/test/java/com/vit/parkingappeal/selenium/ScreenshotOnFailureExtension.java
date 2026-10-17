package com.vit.parkingappeal.selenium;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Failure-screenshot mechanism: runs right after a test method (before @AfterEach closes the browser);
 * if the test threw, saves a PNG to target/screenshots/&lt;TestClass&gt;_&lt;method&gt;_&lt;time&gt;.png
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Object test = context.getRequiredTestInstance();
        if (!(test instanceof BaseSeleniumIT base) || base.driver == null) {
            return;
        }
        WebDriver driver = base.driver;
        try {
            Path dir = Path.of(System.getProperty("screenshotDir", "target/screenshots"));
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path target = dir.resolve(context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getRequiredTestMethod().getName() + "_" + stamp + ".png");
            Path shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(shot, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[screenshot] Test failed - saved " + target.toAbsolutePath()
                    + " (page: " + driver.getCurrentUrl() + ")");
        } catch (IOException | RuntimeException e) {
            System.out.println("[screenshot] Could not capture screenshot: " + e.getMessage());
        }
    }
}
