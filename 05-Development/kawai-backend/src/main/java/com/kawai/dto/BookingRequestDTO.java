package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO gửi lên khi khách bấm "Thanh toán" — gom cả thông tin phòng lẫn thông tin
 * cá nhân.
 * Trước đây, trang Booking gọi API tạo Pending → rồi trang Payment gọi API
 * confirm.
 * Giờ chỉ còn 1 lần gọi duy nhất: khi bấm "Thanh toán" → tạo thẳng
 * Pending_Payment.
 */
public class BookingRequestDTO {

    // ── Thông tin đặt phòng ─────────────────────────────────────────────────
    private Long customerId;
    private List<RoomSelectionDTO> roomSelections;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal depositAmount;
    private String promotionCode; // nullable – UC10.2

    // ── Thông tin khách hàng (điền ở trang Payment trước khi thanh toán) ───
    private String fullName;
    private String phone;
    private String email;
    private String dateOfBirth; // format "yyyy-MM-dd", nullable nếu đã có trong profile
    private String address;
    private String notes;

    private String paymentMethod;

    // ------- Constructors -------
    public BookingRequestDTO() {
    }

    // ------- Getters / Setters (Thông tin đặt phòng) -------
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long v) {
        customerId = v;
    }

    public List<RoomSelectionDTO> getRoomSelections() {
        return roomSelections;
    }

    public void setRoomSelections(List<RoomSelectionDTO> v) {
        roomSelections = v;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate v) {
        checkInDate = v;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate v) {
        checkOutDate = v;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal v) {
        depositAmount = v;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String v) {
        promotionCode = v;
    }

    // ------- Getters / Setters (Thông tin khách hàng) -------
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String v) {
        fullName = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String v) {
        dateOfBirth = v;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String v) {
        address = v;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String v) {
        notes = v;
    }

    // ------- Getters / Setters (Thanh toán) -------
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String v) {
        paymentMethod = v;
    }
}
