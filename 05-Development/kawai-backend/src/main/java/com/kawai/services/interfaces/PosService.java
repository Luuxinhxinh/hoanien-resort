package com.kawai.services.interfaces;

import java.math.BigDecimal;

public interface PosService {
    void chargeToRoom(String roomNumber, BigDecimal amount);

    com.kawai.models.FoodOrder createOrder(com.kawai.dto.CreateFoodOrderRequest request, String userIdentifier);

    com.kawai.models.FoodOrder payOrder(Long id);

    void addItemsToOrder(Long orderId, java.util.List<com.kawai.dto.CartItemDto> items);

    void updateOrderStatus(Long orderId, String status);

    com.kawai.models.FoodOrder getOrderById(Long id);

    /**
     * Hủy đơn hàng F&B (UC19 / WF-24).
     * 
     * @param orderId       ID của đơn hàng cần hủy
     * @param cancelRequest Thông tin hoàn tiền (có thể null nếu không cần refund)
     */
    void cancelOrder(Long orderId, com.kawai.dto.CancelOrderRequestDTO cancelRequest, String cancelledBy, com.kawai.models.Customer explicitCustomer);

    /**
     * Hàm Wrapper xử lý riêng cho trường hợp Khách hàng tự hủy đơn (UC19/WF-24).
     * Mục đích: Tái sử dụng logic cancelOrder(orderId, dto) nhưng có thêm bước kiểm tra
     * quyền sở hữu đơn hàng (ngăn chặn lỗi IDOR - Insecure Direct Object Reference).
     */
    void cancelOrderByGuest(Long orderId, com.kawai.dto.CancelOrderRequestDTO cancelRequest, String username);
}
