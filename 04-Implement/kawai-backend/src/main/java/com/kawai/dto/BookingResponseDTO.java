package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kết quả trả về sau khi tạo booking thành công (UC10).
 */
public class BookingResponseDTO {

    private Long bookingId;
    private String bookingStatus; // "Pending" | "CONFIRMED"
    private BigDecimal depositAmount;
    private BigDecimal discountedPrice;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDate cancellationDeadline; // BR-FIN-02: checkIn - 2 ngày

    // ------- Constructors -------
    public BookingResponseDTO() {
    }

    // ------- Getters / Setters -------
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long v) {
        bookingId = v;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String v) {
        bookingStatus = v;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal v) {
        depositAmount = v;
    }

    public BigDecimal getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(BigDecimal v) {
        discountedPrice = v;
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

    public LocalDate getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDate v) {
        cancellationDeadline = v;
    }
}