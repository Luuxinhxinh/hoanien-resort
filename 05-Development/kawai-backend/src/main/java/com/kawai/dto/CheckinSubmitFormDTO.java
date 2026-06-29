package com.kawai.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO dùng cho form submit Check-in (Web MVC)
 * Gộp chung việc gán phòng (Check-in) và khai báo khách đi kèm (Add
 * Dependents).
 */
public class CheckinSubmitFormDTO {

    private Long bookingId;
    private String guestName;
    private String phone;
    private String cccd;
    private List<String> assignedRoomNumbers = new ArrayList<>();
    private List<java.math.BigDecimal> allocatedCreditLimits = new ArrayList<>();

    // Danh sách người đi kèm, form frontend gửi lên dạng dependents[0].fullName,
    // dependents[1].fullName...
    private List<DependentRegistrationDTO> dependents = new ArrayList<>();
    // Tour allocation: phân bổ tour đã đặt vào phòng vật lý khi check-in
    // (không tạo mới TourBooking, chỉ ghi nhận roomBookingDetail cho từng tour)
    private String tourAllocationMode;
    private List<TourRoomAllocationDTO> tourAllocations = new ArrayList<>();

    // FaceID data for the main Customer
    private String faceVectorData;
    private String faceImageBase64;

    public String getFaceVectorData() {
        return faceVectorData;
    }

    public void setFaceVectorData(String faceVectorData) {
        this.faceVectorData = faceVectorData;
    }

    public String getFaceImageBase64() {
        return faceImageBase64;
    }

    public void setFaceImageBase64(String faceImageBase64) {
        this.faceImageBase64 = faceImageBase64;
    }

    public CheckinSubmitFormDTO() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public List<java.math.BigDecimal> getAllocatedCreditLimits() {
        return allocatedCreditLimits;
    }

    public void setAllocatedCreditLimits(List<java.math.BigDecimal> allocatedCreditLimits) {
        this.allocatedCreditLimits = allocatedCreditLimits;
    }

    public List<String> getAssignedRoomNumbers() {
        return assignedRoomNumbers;
    }

    public void setAssignedRoomNumbers(List<String> assignedRoomNumbers) {
        this.assignedRoomNumbers = assignedRoomNumbers;
    }

    public List<DependentRegistrationDTO> getDependents() {
        return dependents;
    }

    public void setDependents(List<DependentRegistrationDTO> dependents) {
        this.dependents = dependents;
    }

    public String getTourAllocationMode() {
        return tourAllocationMode;
    }

    public void setTourAllocationMode(String tourAllocationMode) {
        this.tourAllocationMode = tourAllocationMode;
    }

    public List<TourRoomAllocationDTO> getTourAllocations() {
        return tourAllocations;
    }

    public void setTourAllocations(List<TourRoomAllocationDTO> tourAllocations) {
        this.tourAllocations = tourAllocations;
    }
}
