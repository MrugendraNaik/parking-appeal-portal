package com.vit.parkingappeal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** A citizen's appeal against a parking violation. */
@Entity
@Table(name = "appeals")
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public reference shown to the citizen, e.g. APL-20261015-K3F9 */
    @Column(nullable = false, unique = true, length = 20)
    private String appealRef;

    @ManyToOne(optional = false)
    @JoinColumn(name = "violation_id")
    private Violation violation;

    @Column(nullable = false, length = 15)
    private String vehicleNumber;

    @Column(nullable = false, length = 60)
    private String ownerName;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 255)
    private String evidenceFileName;

    @Column(nullable = false, length = 255)
    private String evidenceStoredName;

    @Column(nullable = false)
    private long evidenceSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppealStatus status = AppealStatus.SUBMITTED;

    @Column(length = 500)
    private String reviewerRemarks;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime decidedAt;

    protected Appeal() {
        // for JPA
    }

    public Appeal(String appealRef, Violation violation, String vehicleNumber, String ownerName,
                  String email, String phone, String reason, LocalDateTime now) {
        this.appealRef = appealRef;
        this.violation = violation;
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.reason = reason;
        this.submittedAt = now;
        this.updatedAt = now;
    }

    public void attachEvidence(String originalName, String storedName, long size) {
        this.evidenceFileName = originalName;
        this.evidenceStoredName = storedName;
        this.evidenceSize = size;
    }

    public void markUnderReview(LocalDateTime now) {
        if (status == AppealStatus.SUBMITTED) {
            status = AppealStatus.UNDER_REVIEW;
            updatedAt = now;
        }
    }

    public void decide(AppealStatus finalStatus, String remarks, LocalDateTime now) {
        this.status = finalStatus;
        this.reviewerRemarks = remarks;
        this.decidedAt = now;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getAppealRef() { return appealRef; }
    public Violation getViolation() { return violation; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getOwnerName() { return ownerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getReason() { return reason; }
    public String getEvidenceFileName() { return evidenceFileName; }
    public String getEvidenceStoredName() { return evidenceStoredName; }
    public long getEvidenceSize() { return evidenceSize; }
    public AppealStatus getStatus() { return status; }
    public String getReviewerRemarks() { return reviewerRemarks; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
}
