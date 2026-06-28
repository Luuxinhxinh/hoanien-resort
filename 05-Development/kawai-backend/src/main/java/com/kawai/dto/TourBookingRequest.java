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

    @NotNull(message = "roomBookingId is required")
    private Long roomBookingId;
    // Post to Room fields
    private boolean postToRoom = false;
    private Long roomBookingDetailId;

    public TourBookingRequest() {
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

    public Long getRoomBookingId() {
        return roomBookingId;
    }

    public void setRoomBookingId(Long roomBookingId) {
        this.roomBookingId = roomBookingId;
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
}