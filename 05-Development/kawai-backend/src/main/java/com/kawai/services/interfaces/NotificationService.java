package com.kawai.services.interfaces;

public interface NotificationService {
    /**
     * Gửi thông báo đến khách hàng.
     * @param customerId ID khách hàng
     * @param title Tiêu đề
     * @param message Nội dung
     */
    void sendNotification(Long customerId, String title, String message);
}
