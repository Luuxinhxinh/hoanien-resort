package com.kawai.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Hotel_Services")
public class HotelService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long id;

    @Column(name = "service_name", unique = true, nullable = false, length = 150)
    private String serviceName;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "source_department", nullable = false, length = 50)
    private String sourceDepartment;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Getters / Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getSourceDepartment() {
        return sourceDepartment;
    }

    public void setSourceDepartment(String sourceDepartment) {
        this.sourceDepartment = sourceDepartment;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
