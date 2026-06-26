package com.kawai.models;

import jakarta.persistence.*;

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
    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints = 0;
    @Column(name = "membership_tier", nullable = false)
    private String membershipTier = "Regular";
    @Column(name = "avatar_url")
    private String avatarUrl;

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

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer v) {
        this.loyaltyPoints = v;
    }

    public String getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(String v) {
        this.membershipTier = v;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}