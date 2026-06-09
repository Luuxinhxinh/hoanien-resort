package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Accounts") @Data
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="account_id") private Long id;
    @Column(unique=true, nullable=false) private String username;
    @Column(name="password_hash", nullable=false) private String passwordHash;
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
    @ManyToOne @JoinColumn(name="role_id", nullable=false) private Role role;
    @Column(name="created_at") private LocalDateTime createdAt = LocalDateTime.now();
}
