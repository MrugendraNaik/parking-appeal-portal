package com.vit.parkingappeal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 3: the citizen tracks an appeal using reference + email. */
class J3_TrackStatusIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Citizen tracks a submitted appeal; wrong email is refused")
    void citizenTracksAppeal() {
        String email = TestJourneyData.uniqueEmail();
        String ref = submitValidAppeal(freshTicket(), email);

        open("/track");
        type("ref", ref);
        type("email", email);
        byId("trackBtn").click();
        assertThat(byId("appealStatus").getText()).isEqualToIgnoringCase("Submitted");

        open("/track");
        type("ref", ref);
        type("email", "intruder@example.com");
        byId("trackBtn").click();
        assertThat(byId("trackError").getText()).contains("No appeal matches");
    }
}
