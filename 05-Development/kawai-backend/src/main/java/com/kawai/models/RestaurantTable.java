package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Restaurant_Tables") @Data
public class RestaurantTable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="table_id") private Long id;
    @Column(name="table_number", unique=true, nullable=false) private String tableNumber;
    @Column(nullable=false) private Integer capacity;
    @Column(name="table_status", nullable=false) private String tableStatus = "Vacant";
    @Column(name="is_active", nullable=false) private Boolean isActive = true;
}
