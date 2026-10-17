package com.vit.parkingappeal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 2: invalid data and missing documents are rejected with clear messages. */
class J2_ValidationIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Short reason and missing evidence show field errors")
    void invalidAppealShowsErrors() {
        fillAppealForm(freshTicket(), TestJourneyData.uniqueEmail(), TestJourneyData.SHORT_REASON, null);

        assertThat(byId("formErrors").getText()).contains("highlighted");
        assertThat(byId("error-reason").getText()).contains("30 to 1000 characters");
        assertThat(byId("error-evidence").getText()).contains("Upload supporting evidence");
        assertThat(driver.findElements(By.id("appealRef"))).isEmpty();
    }

    @Test
    @DisplayName("Vehicle number that does not match the ticket is refused")
    void vehicleMismatchIsRejected() {
        Ticket t = freshTicket();
        Ticket wrongVehicle = new Ticket(t.ticketNumber(), "MH99ZZ0001");
        fillAppealForm(wrongVehicle, TestJourneyData.uniqueEmail(), TestJourneyData.VALID_REASON, sampleEvidencePdf());

        assertThat(byId("error-vehicleNumber").getText()).contains("does not match");
    }
}
