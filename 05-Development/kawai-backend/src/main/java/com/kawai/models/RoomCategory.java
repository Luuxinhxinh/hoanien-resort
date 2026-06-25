package com.kawai.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "Room_Categories")
@Getter
@Setter
public class RoomCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "base_adults", nullable = false)
    private Integer baseAdults = 2;

    @Column(name = "base_children", nullable = false)
    private Integer baseChildren = 0;

    @Column(name = "max_adults", nullable = false)
    private Integer maxAdults = 2;

    @Column(name = "max_children", nullable = false)
    private Integer maxChildren = 1;
    @Column(name = "cover_img_url", length = 500)
    private String coverImgUrl;

    @Column(name = "extra_adult_surcharge")
    private BigDecimal extraAdultSurcharge;

    @Column(name = "extra_child_surcharge")
    private BigDecimal extraChildSurcharge;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // --- NEW ATTRIBUTES FOR ROOM DISPLAY ---
    @Column(name = "bed_type", length = 100)
    private String bedType; // VD: "1 Giường King 2m2", "2 Giường Đơn 1m2"

    @Column(name = "room_size")
    private Integer roomSize; // Diện tích (m2)

    @Column(name = "view_type", length = 100)
    private String viewType; // Hướng nhìn (VD: "Hướng Biển", "Hướng Vườn")

    @Column(name = "has_bathtub")
    private Boolean hasBathtub = false;

    @Column(name = "has_balcony")
    private Boolean hasBalcony = false;

    @Column(name = "complimentary_services", length = 255)
    private String complimentaryServices; // VD: "2 chai nước suối, Trà & Cà phê"

    @Column(name = "has_free_breakfast")
    private Boolean hasFreeBreakfast = false;

}