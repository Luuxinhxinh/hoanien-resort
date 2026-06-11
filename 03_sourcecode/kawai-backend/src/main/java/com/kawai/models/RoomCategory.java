package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Room_Categories") @Data
public class RoomCategory {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="category_id") private Long id;
    @Column(name="category_name", nullable=false) private String categoryName;
    @Column(name="base_price", nullable=false) private BigDecimal basePrice;
    @Column(nullable=false) private Integer capacity;
}
