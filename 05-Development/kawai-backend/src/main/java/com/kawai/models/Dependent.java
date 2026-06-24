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

    public void setCccdPassportEncrypted(String v) {
        this.cccdPassportEncrypted = v;
    }
}