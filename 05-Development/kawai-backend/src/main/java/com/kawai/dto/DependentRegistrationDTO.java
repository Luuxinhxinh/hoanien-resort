package com.kawai.dto;

import java.time.LocalDate;

/**
 * DTO đầu vào khi đăng ký Dependent (UC16).
 * PII: fullName, dateOfBirth, cccd — phải xử lý đúng theo Nghị định 13/2023.
 */
public class DependentRegistrationDTO {

    private String fullName;
    private LocalDate dateOfBirth;
    private String cccd;       // Raw CCCD — sẽ mã hoá AES-256 trong Service (BR-SYS-01)
    private String gender;
    private String contactInfo; // Optional

    public DependentRegistrationDTO() {
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
}
