package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.envers.Audited;

@Entity
@Table(name = "Accounts")
@Audited
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @org.hibernate.envers.Audited(targetAuditMode = org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED)
    private Role role;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "lockout_time")
    private LocalDateTime lockoutTime;

    @Column(name = "two_factor_code")
    private String twoFactorCode;

    @Column(name = "two_factor_expiry")
    private LocalDateTime twoFactorExpiry;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;

    // Getters and Setters for new fields
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockoutTime() {
        return lockoutTime;
    }

    public void setLockoutTime(LocalDateTime lockoutTime) {
        this.lockoutTime = lockoutTime;
    }

    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }

    public LocalDateTime getTwoFactorExpiry() {
        return twoFactorExpiry;
    }

    public void setTwoFactorExpiry(LocalDateTime twoFactorExpiry) {
        this.twoFactorExpiry = twoFactorExpiry;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordExpiry() {
        return resetPasswordExpiry;
    }

    public void setResetPasswordExpiry(LocalDateTime resetPasswordExpiry) {
        this.resetPasswordExpiry = resetPasswordExpiry;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        this.username = v;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String v) {
        this.passwordHash = v;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean v) {
        this.isActive = v;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role v) {
        this.role = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }
}