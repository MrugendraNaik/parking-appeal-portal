package com.vit.parkingappeal.service;

/** Data the citizen types into the appeal form (the evidence file travels separately). */
public class AppealForm {

    private String ticketNumber;
    private String vehicleNumber;
    private String ownerName;
    private String email;
    private String phone;
    private String reason;

    public AppealForm() {
    }

    public AppealForm(String ticketNumber, String vehicleNumber, String ownerName,
                      String email, String phone, String reason) {
        this.ticketNumber = ticketNumber;
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.reason = reason;
    }

    /** Upper-cases ticket/vehicle, strips spaces and hyphens from the vehicle number, trims text. */
    public AppealForm normalised() {
        return new AppealForm(
                trim(ticketNumber).toUpperCase(),
                trim(vehicleNumber).toUpperCase().replaceAll("[\\s-]", ""),
                trim(ownerName),
                trim(email).toLowerCase(),
                trim(phone).replaceAll("\\s", ""),
                trim(reason));
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
