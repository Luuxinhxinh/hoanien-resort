package com.kawai.dto;

import java.time.LocalDate;

/**
 * DTO đầu vào khi đăng ký Dependent (UC16).
 * PII: fullName, dateOfBirth, cccd — phải xử lý đúng theo Nghị định 13/2023.
 */
public class DependentRegistrationDTO {

    private Long dependentId;
    private Long roomBookingDetailId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String cccd;
    private String gender;
    private String contactInfo;
    private String assignedPhysicalRoomNumber;
    private Boolean isPrimaryContact = false;
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

    public Boolean getIsPrimaryContact() {
        return isPrimaryContact;
    }

    public void setIsPrimaryContact(Boolean isPrimaryContact) {
        this.isPrimaryContact = isPrimaryContact;
    }
    public DependentRegistrationDTO() {
    }

    public Long getDependentId() {
        return dependentId;
    }

    public void setDependentId(Long dependentId) {
        this.dependentId = dependentId;
    }

    public Long getRoomBookingDetailId() {
        return roomBookingDetailId;
    }

    public void setRoomBookingDetailId(Long roomBookingDetailId) {
        this.roomBookingDetailId = roomBookingDetailId;
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

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getAssignedPhysicalRoomNumber() {
        return assignedPhysicalRoomNumber;
    }

    public void setAssignedPhysicalRoomNumber(String assignedPhysicalRoomNumber) {
        this.assignedPhysicalRoomNumber = assignedPhysicalRoomNumber;
    }
}
