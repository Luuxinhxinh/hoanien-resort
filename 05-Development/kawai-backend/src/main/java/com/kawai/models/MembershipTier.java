package com.kawai.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "membership_tiers")
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tier_id")
    private Long id;

    @Column(name = "tier_name", nullable = false, unique = true)
    private String tierName;

    @Column(name = "points_from", nullable = false)
    private Integer pointsFrom;

    @Column(name = "points_to", nullable = false)
    private Integer pointsTo;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "description")
    private String description;

    // Constructors
    public MembershipTier() {
    }

    public MembershipTier(String tierName, Integer pointsFrom, Integer pointsTo, BigDecimal creditLimit, String description) {
        this.tierName = tierName;
        this.pointsFrom = pointsFrom;
        this.pointsTo = pointsTo;
        this.creditLimit = creditLimit;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public Integer getPointsFrom() {
        return pointsFrom;
    }

    public void setPointsFrom(Integer pointsFrom) {
        this.pointsFrom = pointsFrom;
    }

    public Integer getPointsTo() {
        return pointsTo;
    }

    public void setPointsTo(Integer pointsTo) {
        this.pointsTo = pointsTo;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
