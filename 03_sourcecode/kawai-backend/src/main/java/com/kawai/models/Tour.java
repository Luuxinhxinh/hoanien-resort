package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Tours") @Data
public class Tour {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="tour_id") private Long id;
    @Column(name="tour_name", nullable=false) private String tourName;
    @Column(name="tour_type", nullable=false) private String tourType;
    @Column(name="base_price", nullable=false) private BigDecimal basePrice;
    @Column(name="max_capacity", nullable=false) private Integer maxCapacity = 30;
    @Column(columnDefinition="TEXT") private String description;
}
