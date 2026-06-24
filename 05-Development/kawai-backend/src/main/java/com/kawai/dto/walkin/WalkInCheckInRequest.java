package com.kawai.dto.walkin;

import com.kawai.dto.DependentRegistrationDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO nhận dữ liệu Walk-in Check-in từ Lễ tân (UC-14).
 */
public class WalkInCheckInRequest {

    // ── Thông tin khách chính ────────────────────────────────────────────────
    private String fullName;
    private LocalDate dateOfBirth;
    /** CCCD/Hộ chiếu — phải đúng format 12 chữ số nếu được cung cấp */
    private String cccd;
    private String phone;
    private String email;
    private String gender;

    // ── Khách đi kèm và Danh sách phòng ──────────────────────────────────────
    private List<WalkInRoomSelectionDTO> roomSelections;

    // ── Thông tin đặt phòng ──────────────────────────────────────────────────
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private java.math.BigDecimal depositAmount;
    private String paymentMethod;

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String v) {
        this.fullName = v;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate v) {
        this.dateOfBirth = v;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String v) {
        this.cccd = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        this.phone = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String v) {
        this.gender = v;
    }

    public List<WalkInRoomSelectionDTO> getRoomSelections() {
        return roomSelections;
    }

    public void setRoomSelections(List<WalkInRoomSelectionDTO> v) {
        this.roomSelections = v;
    }

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

    public java.math.BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(java.math.BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
