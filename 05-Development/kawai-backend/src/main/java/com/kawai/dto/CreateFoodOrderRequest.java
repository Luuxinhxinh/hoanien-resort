package com.kawai.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateFoodOrderRequest {
    private String orderType; // "Room Service" or "Dine In"
    private String roomNumber;
    private Long tableId;
    private String paymentType;
    private String note;
    private List<CartItemDto> items;
    private Boolean isPaid;
    private String guestName;
}
