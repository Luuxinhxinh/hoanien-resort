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
    @Column(name="duration_hours", nullable=false) private Double durationHours = 2.0;
    @Column(name="is_insurance_required", nullable=false) private Boolean isInsuranceRequired = false;
    @Column(name="insurance_price", nullable=false) private BigDecimal insurancePrice = BigDecimal.ZERO;
    @Column(name="image_url", length = 500) private String imageUrl;
    @Column(name="short_quote") private String shortQuote;
    @Column(name="handbook_spec") private String handbookSpec;
    @Column(name="handbook_logistics", columnDefinition="TEXT") private String handbookLogistics;
    @Column(name="handbook_explanations", columnDefinition="TEXT") private String handbookExplanations;
    @Column(name="created_at") private java.time.LocalDateTime createdAt;
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
}
