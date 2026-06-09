package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity @Table(name="Promotions") @Data
public class Promotion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="promo_id") private Long id;
    @Column(name="promo_code", unique=true, nullable=false) private String promoCode;
    @Column(name="discount_type", nullable=false) private String discountType;
    @Column(name="discount_value", nullable=false) private BigDecimal discountValue;
    @Column(name="valid_from", nullable=false) private LocalDateTime validFrom;
    @Column(name="valid_to", nullable=false) private LocalDate validTo;
    @Column(name="max_uses", nullable=false) private Integer maxUses;
    @Column(name="current_uses", nullable=false) private Integer currentUses = 0;
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
}
