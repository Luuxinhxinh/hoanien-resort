package com.kawai.dto;

import java.time.LocalDate;

/**
 * DTO trả về sau khi đăng ký Dependent thành công (UC16).
 * status: "REGISTERED" | "AUTHORIZED"
 */
public class DependentResponseDTO {

    private Long dependentId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String status; // REGISTERED | AUTHORIZED
    private Boolean isPrimaryContact;
    private String assignedRoom;
    private String cccd;
    private String gender;
    private Long roomBookingDetailId;

    public DependentResponseDTO() {
    }

    public Long getDependentId() {
        return dependentId;
    }

    public void setDependentId(Long dependentId) {
        this.dependentId = dependentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsPrimaryContact() {
        return isPrimaryContact;
    }

    public void setIsPrimaryContact(Boolean isPrimaryContact) {
        this.isPrimaryContact = isPrimaryContact;
    }

    public String getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(String assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getRoomBookingDetailId() {
        return roomBookingDetailId;
    }

    public void setRoomBookingDetailId(Long roomBookingDetailId) {
        this.roomBookingDetailId = roomBookingDetailId;
    }
}
