package com.kawai.services.interfaces;

import java.math.BigDecimal;

public interface PosService {
    void chargeToRoom(String roomNumber, BigDecimal amount);
    
    com.kawai.models.FoodOrder createOrder(com.kawai.dto.CreateFoodOrderRequest request, String userIdentifier);
    
    com.kawai.models.FoodOrder payOrder(Long id);
    
    void addItemsToOrder(Long orderId, java.util.List<com.kawai.dto.CartItemDto> items);

    void updateOrderStatus(Long orderId, String status);

    /**
     * Hủy đơn hàng F&B (UC19 / WF-24).
     * @param orderId ID của đơn hàng cần hủy
     * @param cancelRequest Thông tin hoàn tiền (có thể null nếu không cần refund)
     */
    void cancelOrder(Long orderId, com.kawai.dtos.CancelOrderRequestDTO cancelRequest);
}
