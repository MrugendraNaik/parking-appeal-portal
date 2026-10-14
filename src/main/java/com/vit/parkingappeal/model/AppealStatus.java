package com.vit.parkingappeal.model;

/** Lifecycle: SUBMITTED -> UNDER_REVIEW -> APPROVED | REJECTED */
public enum AppealStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under review"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String label;

    AppealStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** CSS modifier used by the status pill, e.g. "under_review". */
    public String getCss() {
        return name().toLowerCase();
    }

    public boolean isSubmitted() {
        return this == SUBMITTED;
    }

    /** A final status cannot be changed again. */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
}
