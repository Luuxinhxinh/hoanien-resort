package com.kawai.models;

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

    // GiÃ¡ Ä‘Ã£ Ä‘Æ°á»£c há»‡ thá»‘ng gen sáºµn (tÃ­nh toÃ¡n tá»« DynamicPricing + BasePrice)
    @Column(name = "computed_price", nullable = false)
    private BigDecimal computedPrice;

    @Column(name = "is_weekend", nullable = false)
    private Boolean isWeekend = false;

    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = false;
}
