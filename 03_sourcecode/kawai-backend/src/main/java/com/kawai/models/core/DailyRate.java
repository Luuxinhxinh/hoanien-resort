package com.kawai.models.core;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Daily_Rates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"category_id", "rate_date"})
})
@Data
public class DailyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_rate_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private RoomCategory category;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    // Giá đã được hệ thống gen sẵn (tính toán từ DynamicPricing + BasePrice)
    @Column(name = "computed_price", nullable = false)
    private BigDecimal computedPrice;

    @Column(name = "is_weekend", nullable = false)
    private Boolean isWeekend = false;

    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = false;
}
