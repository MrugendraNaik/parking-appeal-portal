package com.vit.parkingappeal.selenium;

/** Test data used by the Selenium journeys. */
final class TestJourneyData {

    static final String VALID_REASON =
            "The no-parking board was hidden behind a parked truck and a tree branch. Photo of the spot attached.";
    static final String SHORT_REASON = "Not my fault";
    static final String REJECTION_REMARKS = "Evidence photo clearly shows a visible no-parking sign.";
    static final String APPROVAL_REMARKS = "Signage was obstructed; fine cancelled.";

    private TestJourneyData() {
    }

    static String uniqueEmail() {
        return "citizen" + System.nanoTime() % 1_000_000 + "@example.com";
    }
}
