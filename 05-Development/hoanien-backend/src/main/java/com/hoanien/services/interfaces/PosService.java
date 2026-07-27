package com.hoanien.services.interfaces;

import java.math.BigDecimal;

public interface PosService {
    void chargeToRoom(String roomNumber, BigDecimal amount);

    java.util.Map<String, Object> verifyRoomForCharge(String roomNumber);

    com.hoanien.models.FoodOrder createOrder(com.hoanien.dto.CreateFoodOrderRequest request, String userIdentifier);

    com.hoanien.models.FoodOrder payOrder(Long id, String paymentType, String roomNumber);

    void addItemsToOrder(Long orderId, java.util.List<com.hoanien.dto.CartItemDto> items);

    void updateOrderStatus(Long orderId, String status);

    com.hoanien.models.FoodOrder getOrderById(Long id);

    /**
     * Hủy đơn hàng F&B (UC19 / WF-24).
     * 
     * @param orderId       ID của đơn hàng cần hủy
     * @param cancelRequest Thông tin hoàn tiền (có thể null nếu không cần refund)
     */
    void cancelOrder(Long orderId, com.hoanien.dto.CancelOrderRequestDTO cancelRequest, String cancelledBy, com.hoanien.models.Customer explicitCustomer);

    /**
     * Hàm Wrapper xử lý riêng cho trường hợp Khách hàng tự hủy đơn (UC19/WF-24).
     * Mục đích: Tái sử dụng logic cancelOrder(orderId, dto) nhưng có thêm bước kiểm tra
     * quyền sở hữu đơn hàng (ngăn chặn lỗi IDOR - Insecure Direct Object Reference).
     */
    void cancelOrderByGuest(Long orderId, com.hoanien.dto.CancelOrderRequestDTO cancelRequest, String username);
}
