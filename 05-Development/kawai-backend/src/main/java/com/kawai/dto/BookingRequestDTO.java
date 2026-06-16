package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO dùng khi khách hàng gửi yêu cầu đặt phòng (UC10).
 */
public class BookingRequestDTO {

    private Long customerId;
    private String roomNumber;      // Ví dụ "R101"
    private String roomCategoryName; // nullable - dùng để tự động chọn phòng trống
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal depositAmount;
    private String promotionCode;   // nullable – UC10.2

    // ------- Constructors -------
    public BookingRequestDTO() {}

    public BookingRequestDTO(Long customerId, String roomNumber,
                              LocalDate checkInDate, LocalDate checkOutDate,
                              BigDecimal depositAmount) {
        this.customerId    = customerId;
        this.roomNumber    = roomNumber;
        this.checkInDate   = checkInDate;
        this.checkOutDate  = checkOutDate;
        this.depositAmount = depositAmount;
    }

    // ------- Getters / Setters -------
    public Long getCustomerId()               { return customerId; }
    public void setCustomerId(Long v)         { customerId = v; }

    public String getRoomNumber()             { return roomNumber; }
    public void setRoomNumber(String v)       { roomNumber = v; }

    public LocalDate getCheckInDate()         { return checkInDate; }
    public void setCheckInDate(LocalDate v)   { checkInDate = v; }

    public LocalDate getCheckOutDate()        { return checkOutDate; }
    public void setCheckOutDate(LocalDate v)  { checkOutDate = v; }

    public BigDecimal getDepositAmount()            { return depositAmount; }
    public void setDepositAmount(BigDecimal v)       { depositAmount = v; }

    public String getPromotionCode()          { return promotionCode; }
    public void setPromotionCode(String v)    { promotionCode = v; }

    public String getRoomCategoryName()       { return roomCategoryName; }
    public void setRoomCategoryName(String v) { roomCategoryName = v; }
}
