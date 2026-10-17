package com.vit.parkingappeal.web;

import com.vit.parkingappeal.model.Appeal;

import java.time.LocalDateTime;

/** JSON shape returned by the REST API (keeps entities out of the API contract). */
public record AppealSummary(String appealRef, String ticketNumber, String vehicleNumber, String ownerName,
                            String status, String reviewerRemarks, LocalDateTime submittedAt,
                            LocalDateTime decidedAt) {

    public static AppealSummary of(Appeal a) {
        return new AppealSummary(a.getAppealRef(), a.getViolation().getTicketNumber(), a.getVehicleNumber(),
                a.getOwnerName(), a.getStatus().name(), a.getReviewerRemarks(), a.getSubmittedAt(),
                a.getDecidedAt());
    }
}
