package com.vit.parkingappeal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 1: a citizen files a valid appeal and receives a reference number. */
class J1_SubmitAppealIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Citizen submits a valid appeal and gets an APL reference")
    void citizenSubmitsValidAppeal() {
        open("/");
        byId("startAppealBtn").click();
        assertThat(driver.getCurrentUrl()).contains("/appeals/new");

        String ref = submitValidAppeal(freshTicket(), TestJourneyData.uniqueEmail());

        assertThat(ref).matches("APL-\\d{8}-[A-Z2-9]{4}");
        assertThat(byId("confirmStatus").getText()).isEqualToIgnoringCase("Submitted");
    }
}
