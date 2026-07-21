package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Dependents")
public class Dependent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dependent_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "dependent_name")
    private String dependentName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column
    private String gender;

    @Column(name = "cccd_passport_encrypted")
    private String cccdPassportEncrypted;

    @Column(name = "face_vector_data", columnDefinition = "TEXT")
    private String faceVectorData;

    @Column(name = "face_img_url", length = 500)
    private String faceImgUrl;

    public String getFaceVectorData() {
        return faceVectorData;
    }

    public void setFaceVectorData(String faceVectorData) {
        this.faceVectorData = faceVectorData;
    }

    public String getFaceImgUrl() {
        return faceImgUrl;
    }

    public void setFaceImgUrl(String faceImgUrl) {
        this.faceImgUrl = faceImgUrl;
    }

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer v) {
        this.customer = v;
    }

    public String getDependentName() {
        return dependentName;
    }

    public void setDependentName(String v) {
        this.dependentName = v;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate v) {
        this.birthDate = v;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String v) {
        this.gender = v;
    }

    public String getCccdPassportEncrypted() {
        return cccdPassportEncrypted;
    }

    public String getCccdPassportDecrypted() {
        if (cccdPassportEncrypted == null || cccdPassportEncrypted.isBlank()) return "--";
        if (cccdPassportEncrypted.startsWith("PHONE_") || cccdPassportEncrypted.startsWith("AUTO_CHILD_")) return "--";
        return com.kawai.utils.EncryptionUtils.decrypt(cccdPassportEncrypted);
    }

    public void setCccdPassportEncrypted(String v) {
        this.cccdPassportEncrypted = v;
    }
}