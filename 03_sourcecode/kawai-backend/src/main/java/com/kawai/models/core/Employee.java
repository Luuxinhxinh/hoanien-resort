package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Employees") @Data
public class Employee {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="employee_id") private Long id;
    @OneToOne @JoinColumn(name="account_id", unique=true) private Account account;
    @Column(name="full_name", nullable=false) private String fullName;
    @Column(nullable=false) private String gender;
    @Column(unique=true, nullable=false) private String cccd;
    @Column(nullable=false) private String phone;
    @Column(unique=true, nullable=false) private String email;
    @Column(nullable=false) private BigDecimal salary;
}
