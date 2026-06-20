package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import org.hibernate.envers.Audited;

@Entity 
@Audited 
@Table(name="Menu_Items") 
@Data
public class MenuItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="item_id") private Long id;
    @Column(name="item_name", nullable=false) private String itemName;
    @Column(nullable=false) private BigDecimal price;
    @Column(nullable=false) private String category;
    @Column(name="is_available", nullable=false) private Boolean isAvailable = true;
    @Column(columnDefinition="TEXT") private String description;
    @Column(name="image_url") private String imageUrl;
    @Column(name="allergy_tags") private String allergenTags;
}

