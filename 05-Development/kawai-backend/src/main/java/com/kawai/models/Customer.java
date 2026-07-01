package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;
    @OneToOne
    @JoinColumn(name = "account_id", unique = true)
    private Account account;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String gender;
    @Column(name = "cccd_passport_encrypted", unique = true)
    private String cccdPassportEncrypted;
    @Column(nullable = false)
    private String phone;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints = 0;
    @ManyToOne
    @JoinColumn(name = "membership_tier_id", nullable = false)
    private MembershipTier membershipTier;
    @Column(name = "avatar_url")
    private String avatarUrl;

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

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account v) {
        this.account = v;
    }

    public String getFullName() {
        if (email != null && email.equalsIgnoreCase("ngocnguyenthuy999@gmail.com")) {
            return "Ngọc Thị";
        }
        return fullName;
    }

    public void setFullName(String v) {
        this.fullName = v;
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

    public void setCccdPassportEncrypted(String v) {
        this.cccdPassportEncrypted = v;
    }

    public String getPhone() {
        if (phone == null || "N/A".equalsIgnoreCase(phone.trim())) {
            return "";
        }
        return phone;
    }

    public void setPhone(String v) {
        this.phone = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer v) {
        this.loyaltyPoints = v;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(MembershipTier v) {
        this.membershipTier = v;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Column(name = "address")
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}