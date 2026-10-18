package com.vit.parkingappeal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 4: reviewer approves an appeal and the citizen sees APPROVED. */
class J4_ReviewerApproveIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Reviewer approves; citizen sees Approved with remarks")
    void reviewerApprovesAndCitizenSeesIt() {
        String email = TestJourneyData.uniqueEmail();
        String ref = submitValidAppeal(freshTicket(), email);

        reviewerLogin();
        byId("open-" + ref).click();
        assertThat(byId("detailStatus").getText()).isEqualToIgnoringCase("Under review");
        type("remarks", TestJourneyData.APPROVAL_REMARKS);
        byId("approveBtn").click();
        assertThat(byId("flashSuccess").getText()).contains("approved");

        open("/track?ref=" + ref + "&email=" + email);
        assertThat(byId("appealStatus").getText()).isEqualToIgnoringCase("Approved");
        assertThat(byId("reviewerRemarks").getText()).isEqualTo(TestJourneyData.APPROVAL_REMARKS);
    }
}
