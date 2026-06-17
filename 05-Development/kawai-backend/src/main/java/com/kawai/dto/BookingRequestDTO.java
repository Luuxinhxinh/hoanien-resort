package com.kawai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO dùng khi khách hàng gửi yêu cầu đặt phòng (UC10).
 */
public class BookingRequestDTO {

    private Long customerId;
    private List<RoomSelectionDTO> roomSelections;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal depositAmount;
    private String promotionCode;   // nullable – UC10.2

    // ------- Constructors -------
    public BookingRequestDTO() {}

    public BookingRequestDTO(Long customerId, List<RoomSelectionDTO> roomSelections,
                              LocalDate checkInDate, LocalDate checkOutDate,
                              BigDecimal depositAmount) {
        this.customerId    = customerId;
        this.roomSelections   = roomSelections;
        this.checkInDate   = checkInDate;
        this.checkOutDate  = checkOutDate;
        this.depositAmount = depositAmount;
    }

    // ------- Getters / Setters -------
    public Long getCustomerId()               { return customerId; }
    public void setCustomerId(Long v)         { customerId = v; }

    public List<RoomSelectionDTO> getRoomSelections()      { return roomSelections; }
    public void setRoomSelections(List<RoomSelectionDTO> v){ roomSelections = v; }

    public LocalDate getCheckInDate()         { return checkInDate; }
    public void setCheckInDate(LocalDate v)   { checkInDate = v; }

    public LocalDate getCheckOutDate()        { return checkOutDate; }
    public void setCheckOutDate(LocalDate v)  { checkOutDate = v; }

    public BigDecimal getDepositAmount()            { return depositAmount; }
    public void setDepositAmount(BigDecimal v)       { depositAmount = v; }

    public String getPromotionCode()          { return promotionCode; }
    public void setPromotionCode(String v)    { promotionCode = v; }
}
