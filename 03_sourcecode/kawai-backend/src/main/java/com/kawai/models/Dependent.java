package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name="Dependents") @Data
public class Dependent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="dependent_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @Column(name="dependent_name", nullable=false) private String dependentName;
    @Column(name="birth_date", nullable=false) private LocalDate birthDate;
    @Column(nullable=false) private String gender;
    @Column(name="cccd_passport_encrypted") private String cccdPassportEncrypted;
}
