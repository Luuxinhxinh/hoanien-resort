package com.kawai.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promo_id")
    private Long id;
    @Column(name = "promo_code", unique = true, nullable = false)
    private String promoCode;
    @Column(name = "discount_type", nullable = false)
    private String discountType;
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;
    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    @Column(name = "description", length = 1000)
    private String description;
    @Column(name = "combo_config", columnDefinition = "TEXT")
    private String comboConfig;

    public String getComboConfig() {
        return comboConfig;
    }

    public void setComboConfig(String comboConfig) {
        this.comboConfig = comboConfig;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String v) {
        this.promoCode = v;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String v) {
        this.discountType = v;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal v) {
        this.discountValue = v;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime v) {
        this.validFrom = v;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate v) {
        this.validTo = v;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer v) {
        this.maxUses = v;
    }

    public Integer getCurrentUses() {
        return currentUses;
    }

    public void setCurrentUses(Integer v) {
        this.currentUses = v;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean v) {
        this.isActive = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}