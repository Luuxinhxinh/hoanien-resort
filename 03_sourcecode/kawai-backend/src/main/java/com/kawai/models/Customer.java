package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Customers") @Data
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="customer_id") private Long id;
    @OneToOne @JoinColumn(name="account_id", unique=true) private Account account;
    @Column(name="full_name", nullable=false) private String fullName;
    @Column(nullable=false) private String gender;
    @Column(name="cccd_passport_encrypted", unique=true) private String cccdPassportEncrypted;
    @Column(nullable=false) private String phone;
    @Column(unique=true, nullable=false) private String email;
    @Column(name="loyalty_points", nullable=false) private Integer loyaltyPoints = 0;
    @Column(name="membership_tier", nullable=false) private String membershipTier = "Regular";
}
