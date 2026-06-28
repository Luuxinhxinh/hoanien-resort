package com.kawai.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho UC20.1: Đặt tour du lịch.
 *
 * <p>
 * Chứa thông tin cần thiết để khách hàng đặt một tour.
 * Hỗ trợ cả hình thức thanh toán: trả trực tiếp hoặc Post to Room (ghi nợ
 * phòng).
 *
 * @see com.kawai.services.interfaces.TourBookingService
 */
public class TourBookingRequest {

    @NotNull(message = "scheduleId is required")
    private Long scheduleId;

    @NotNull(message = "customerId is required")
    private Long customerId;

    @Min(value = 1, message = "participantCount must be at least 1")
    private int participantCount;

    private boolean isWalkInTour = false;

    // Post to Room fields
    private boolean postToRoom = false;
    private Long roomBookingDetailId;

    // Payment method: "counter", "post-room", "vnpay"
    private String paymentMethod = "counter";

    private java.util.List<String> childAges;

    // Mã giảm giá (tùy chọn)
    private String promoCode;

    public TourBookingRequest() {
    }

    public java.util.List<String> getChildAges() {
        return childAges;
    }

    public void setChildAges(java.util.List<String> childAges) {
        this.childAges = childAges;
    }

    // --- Getters & Setters ---

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public boolean isWalkInTour() {
        return isWalkInTour;
    }

    public void setWalkInTour(boolean walkInTour) {
        isWalkInTour = walkInTour;
    }

    public boolean isPostToRoom() {
        return postToRoom;
    }

    public void setPostToRoom(boolean postToRoom) {
        this.postToRoom = postToRoom;
    }

    public Long getRoomBookingDetailId() {
        return roomBookingDetailId;
    }

    public void setRoomBookingDetailId(Long roomBookingDetailId) {
        this.roomBookingDetailId = roomBookingDetailId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }
}