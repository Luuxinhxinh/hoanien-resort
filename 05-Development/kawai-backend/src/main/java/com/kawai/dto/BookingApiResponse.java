package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingApiResponse {
    private String status;
    private String bookingStatus;
    private Long bookingId;
    private BigDecimal depositAmount;
    private LocalDateTime cancellationDeadline;
    private String message;
    private String paymentUrl;
    private BigDecimal discountedPrice;

    public BookingApiResponse() {
    }

    public BookingApiResponse(String status, String bookingStatus, Long bookingId, BigDecimal depositAmount,
            LocalDateTime cancellationDeadline, String message) {
        this.status = status;
        this.bookingStatus = bookingStatus;
        this.bookingId = bookingId;
        this.depositAmount = depositAmount;
        this.cancellationDeadline = cancellationDeadline;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public LocalDateTime getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDateTime cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public BigDecimal getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(BigDecimal discountedPrice) {
        this.discountedPrice = discountedPrice;
    }
}
