package com.vit.parkingappeal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 5: rejection requires remarks; then the appeal is closed as REJECTED. */
class J5_ReviewerRejectIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Reject without remarks fails; with remarks the appeal is rejected")
    void rejectionNeedsRemarks() {
        String ref = submitValidAppeal(freshTicket(), TestJourneyData.uniqueEmail());

        reviewerLogin();
        byId("open-" + ref).click();
        byId("rejectBtn").click();
        assertThat(byId("flashError").getText()).contains("reason for rejection");

        type("remarks", TestJourneyData.REJECTION_REMARKS);
        byId("rejectBtn").click();
        assertThat(byId("flashSuccess").getText()).contains("rejected");
        assertThat(byId("detailStatus").getText()).isEqualToIgnoringCase("Rejected");
        assertThat(driver.findElements(org.openqa.selenium.By.id("decisionForm"))).isEmpty();
    }
}
