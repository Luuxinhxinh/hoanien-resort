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
    
    @NotNull(message = "roomBookingId is required")
    private Long roomBookingId;

    // Payment method: "counter", "post-room", "vnpay"
    private String paymentMethod = "counter";

    private java.util.List<String> childAges;

    // Mã giảm giá (tùy chọn)
    private String promoCode;

    private String vnpPaymentType;

    public TourBookingRequest() {
    }

    public String getVnpPaymentType() {
        return vnpPaymentType;
    }

    public void setVnpPaymentType(String vnpPaymentType) {
        this.vnpPaymentType = vnpPaymentType;
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

    public Long getRoomBookingId() {
        return roomBookingId;
    }

    public void setRoomBookingId(Long roomBookingId) {
        this.roomBookingId = roomBookingId;
    }

    private String notes;
    private boolean isCustomerGoing = true;
    private java.util.List<CompanionRequest> companions = new java.util.ArrayList<>();

    public boolean isCustomerGoing() {
        return isCustomerGoing;
    }

    public void setCustomerGoing(boolean customerGoing) {
        isCustomerGoing = customerGoing;
    }

    public java.util.List<CompanionRequest> getCompanions() {
        return companions;
    }

    public void setCompanions(java.util.List<CompanionRequest> companions) {
        this.companions = companions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ─── Bảo hiểm ────────────────────────────────────────────────────────
    /** Khách đồng ý mua bảo hiểm (bắt buộc với tour có isInsuranceRequired=true) */
    private boolean acceptInsurance = false;

    public boolean isAcceptInsurance() {
        return acceptInsurance;
    }

    public void setAcceptInsurance(boolean acceptInsurance) {
        this.acceptInsurance = acceptInsurance;
    }

    // Companion DTO representation
    public static class CompanionRequest {
        private String name;
        private Integer age;
        private String phone;
        private String idCard;
        private Long dependentId;

        public Long getDependentId() {
            return dependentId;
        }

        public void setDependentId(Long dependentId) {
            this.dependentId = dependentId;
        }

        public CompanionRequest() {}

        public CompanionRequest(String name, Integer age, String phone) {
            this.name = name;
            this.age = age;
            this.phone = phone;
        }

        public CompanionRequest(String name, Integer age, String phone, String idCard) {
            this.name = name;
            this.age = age;
            this.phone = phone;
            this.idCard = idCard;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }
}