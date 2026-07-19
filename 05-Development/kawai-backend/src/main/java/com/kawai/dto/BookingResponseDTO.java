package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Kết quả trả về sau khi tạo booking thành công (UC10).
 */
public class BookingResponseDTO {

    private Long bookingId;
    private String bookingStatus;
    private BigDecimal depositAmount;
    private BigDecimal discountedPrice;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime cancellationDeadline;
    private Boolean hasRefundableItems = false;
    private Boolean hasAttachedTours = false;
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

    public LocalDateTime getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDateTime v) {
        cancellationDeadline = v;
    }

    public Boolean getHasRefundableItems() {
        return hasRefundableItems;
    }

    public void setHasRefundableItems(Boolean hasRefundableItems) {
        this.hasRefundableItems = hasRefundableItems;
    }

    public Boolean getHasAttachedTours() {
        return hasAttachedTours;
    }

    public void setHasAttachedTours(Boolean hasAttachedTours) {
        this.hasAttachedTours = hasAttachedTours;
    }
}