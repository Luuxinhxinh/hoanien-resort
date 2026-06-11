package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Accounts")
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
    private Role role;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

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