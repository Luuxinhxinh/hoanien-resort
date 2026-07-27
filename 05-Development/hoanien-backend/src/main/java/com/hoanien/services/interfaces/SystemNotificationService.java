package com.hoanien.services.interfaces;

import com.hoanien.models.Account;
import com.hoanien.models.SystemNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SystemNotificationService {
    
    // Create and emit a notification to a specific account
    SystemNotification createNotification(Account recipient, String title, String message, String type, String targetUrl);
    
    // Helper to send to account by username
    SystemNotification createNotification(String username, String title, String message, String type, String targetUrl);

    // Get unread notifications for a user
    List<SystemNotification> getUnreadNotifications(String username);

    // Get paginated notifications
    Page<SystemNotification> getNotifications(String username, Pageable pageable);

    // Get unread count
    long getUnreadCount(String username);

    // Mark as read
    void markAsRead(Long notificationId, String username);

    // Mark all as read
    void markAllAsRead(String username);
}
