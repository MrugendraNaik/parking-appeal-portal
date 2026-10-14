package com.vit.parkingappeal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** An issued parking ticket (challan). Appeals are validated against these records. */
@Entity
@Table(name = "violations")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(nullable = false, length = 15)
    private String vehicleNumber;

    @Column(nullable = false, length = 60)
    private String violationType;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(nullable = false)
    private LocalDate violationDate;

    @Column(nullable = false)
    private int fineAmount;

    protected Violation() {
        // for JPA
    }

    public Violation(String ticketNumber, String vehicleNumber, String violationType,
                     String location, LocalDate violationDate, int fineAmount) {
        this.ticketNumber = ticketNumber;
        this.vehicleNumber = vehicleNumber;
        this.violationType = violationType;
        this.location = location;
        this.violationDate = violationDate;
        this.fineAmount = fineAmount;
    }

    public Long getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getViolationType() { return violationType; }
    public String getLocation() { return location; }
    public LocalDate getViolationDate() { return violationDate; }
    public int getFineAmount() { return fineAmount; }
}
