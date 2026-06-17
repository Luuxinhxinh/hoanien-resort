package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "Room_Bookings")
public class RoomBooking extends Booking {
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;
    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;
    @Column(name = "cancellation_deadline", nullable = false)
    private LocalDate cancellationDeadline;
    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit = new BigDecimal("5000000.00");
    @Column(name = "personal_pin_hash", nullable = false)
    private String personalPinHash;

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate v) {
        this.checkInDate = v;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate v) {
        this.checkOutDate = v;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal v) {
        this.depositAmount = v;
    }

    public LocalDate getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDate v) {
        this.cancellationDeadline = v;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal v) {
        this.creditLimit = v;
    }

    public String getPersonalPinHash() {
        return personalPinHash;
    }

    public void setPersonalPinHash(String v) {
        this.personalPinHash = v;
    }
}