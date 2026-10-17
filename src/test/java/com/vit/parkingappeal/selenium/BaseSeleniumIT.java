package com.vit.parkingappeal.selenium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Common Selenium setup. Settings come from system properties (set in pom.xml, override with -D):
 *   baseUrl   (default http://localhost:8095)  e.g. -DbaseUrl=http://localhost:8081/parking-appeal
 *   browser   chrome | firefox                 (default chrome)
 *   headless  true | false                     (default true; use false to watch the browser)
 */
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseSeleniumIT {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8095").replaceAll("/$", "");
    protected static final String REVIEWER_KEY = System.getProperty("reviewerKey", "reviewer123");

    protected WebDriver driver;
    protected WebDriverWait wait;

    /** A ticket created just for one test (from the /api/test/violations test-data endpoint). */
    protected record Ticket(String ticketNumber, String vehicleNumber) {
    }

    @BeforeEach
    void openBrowser() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        if ("firefox".equalsIgnoreCase(System.getProperty("browser", "chrome"))) {
            FirefoxOptions o = new FirefoxOptions();
            if (headless) {
                o.addArguments("-headless");
            }
            driver = new FirefoxDriver(o);
        } else {
            ChromeOptions o = new ChromeOptions();
            if (headless) {
                o.addArguments("--headless=new");
            }
            o.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1366,900");
            driver = new ChromeDriver(o);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- helpers ----------

    protected void open(String path) {
        driver.get(BASE_URL + path);
    }

    protected WebElement byId(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
    }

    protected void type(String id, String text) {
        WebElement e = byId(id);
        e.clear();
        e.sendKeys(text);
    }

    /** Asks the app (test-support mode) for a brand-new ticket so every run uses unused data. */
    protected Ticket freshTicket() {
        try {
            HttpResponse<String> res = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(BASE_URL + "/api/test/violations"))
                            .POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 201) {
                throw new IllegalStateException("Test-data endpoint returned " + res.statusCode()
                        + ". Start the app with APP_TEST_SUPPORT=true (or app.test-support=true).");
            }
            JsonNode json = new ObjectMapper().readTree(res.body());
            return new Ticket(json.get("ticketNumber").asText(), json.get("vehicleNumber").asText());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot reach " + BASE_URL + " - is the app running?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected static Path sampleEvidencePdf() {
        try {
            Path p = Files.createTempFile("evidence-", ".pdf");
            Files.writeString(p, "%PDF-1.4\n% Selenium test evidence\n%%EOF\n", StandardCharsets.US_ASCII);
            p.toFile().deleteOnExit();
            return p;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Fills and submits the appeal form. Returns nothing - callers assert on the resulting page. */
    protected void fillAppealForm(Ticket t, String email, String reason, Path evidence) {
        open("/appeals/new");
        type("ticketNumber", t.ticketNumber());
        type("vehicleNumber", t.vehicleNumber());
        type("ownerName", "Selenium Tester");
        type("email", email);
        type("phone", "9876543210");
        type("reason", reason);
        if (evidence != null) {
            driver.findElement(By.id("evidence")).sendKeys(evidence.toAbsolutePath().toString());
        }
        byId("submitAppeal").click();
    }

    /** Full happy-path submission; returns the appeal reference shown on the confirmation page. */
    protected String submitValidAppeal(Ticket t, String email) {
        fillAppealForm(t, email, TestJourneyData.VALID_REASON, sampleEvidencePdf());
        return byId("appealRef").getText().trim();
    }

    protected void reviewerLogin() {
        open("/reviewer/login");
        type("reviewerKey", REVIEWER_KEY);
        byId("loginBtn").click();
        byId("appealsTable");
    }
}
