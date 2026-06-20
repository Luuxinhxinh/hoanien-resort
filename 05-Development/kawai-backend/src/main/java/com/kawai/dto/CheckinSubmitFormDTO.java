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
    private List<String> assignedRoomNumbers = new ArrayList<>();

    // Danh sách người đi kèm, form frontend gửi lên dạng dependents[0].fullName,
    // dependents[1].fullName...
    private List<DependentRegistrationDTO> dependents = new ArrayList<>();

    public CheckinSubmitFormDTO() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
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
}
