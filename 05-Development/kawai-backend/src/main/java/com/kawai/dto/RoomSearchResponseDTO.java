package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Kết quả trả về khi tìm kiếm phòng trống (UC09).
 */
@Getter
@Setter
public class RoomSearchResponseDTO {

    private Long roomId;
    private String roomNumber;
    private String categoryName;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer availableCount;

    private Integer baseAdults;
    private Integer baseChildren;
    private Integer maxAdults;
    private Integer maxChildren;
    private BigDecimal extraAdultSurcharge;
    private BigDecimal extraChildSurcharge;
    private String description;

    // Legacy fields to maintain backwards compatibility
    private Integer beds;
    private Integer size;
    private String view;
    private List<Object> amenities;

    // NEW ROOM CATEGORY FIELDS
    private String bedType;
    private Integer roomSize;
    private String viewType;
    private Boolean hasBathtub;
    private Boolean hasBalcony;
    private String complimentaryServices;
    private Boolean hasFreeBreakfast;
    private String coverImgUrl;
}