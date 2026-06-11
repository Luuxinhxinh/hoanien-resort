package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="Dynamic_Pricing") @Data
public class DynamicPricing {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="price_id") private Long id;
    @ManyToOne @JoinColumn(name="category_id", nullable=false) private RoomCategory category;
    @Column(name="start_date", nullable=false) private LocalDate startDate;
    @Column(name="end_date", nullable=false) private LocalDate endDate;
    @Column(name="price_modifier", nullable=false) private BigDecimal priceModifier;
}
