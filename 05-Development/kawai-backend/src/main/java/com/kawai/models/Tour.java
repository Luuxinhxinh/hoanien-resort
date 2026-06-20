package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import org.hibernate.envers.Audited;

@Entity 
@Audited 
@Table(name="Tours") 
@Data
public class Tour {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="tour_id") private Long id;
    @Column(name="tour_name", nullable=false) private String tourName;
    @Column(name="tour_type", nullable=false) private String tourType;
    @Column(name="base_price", nullable=false) private BigDecimal basePrice;
    @Column(name="max_capacity", nullable=false) private Integer maxCapacity = 30;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="duration") private String duration;
    @Column(name="short_quote") private String shortQuote;
    @Column(name="created_at") private java.time.LocalDateTime createdAt;
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
}
