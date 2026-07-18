package com.kawai.services.impl;

import com.kawai.dto.SystemNotificationDTO;
import com.kawai.models.Account;
import com.kawai.models.SystemNotification;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.SystemNotificationRepository;
import com.kawai.services.interfaces.SystemNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private final SystemNotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SystemNotificationServiceImpl(
            SystemNotificationRepository notificationRepository,
            AccountRepository accountRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.accountRepository = accountRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public SystemNotification createNotification(Account recipient, String title, String message, String type, String targetUrl) {
        SystemNotification notification = new SystemNotification(recipient, title, message, type, targetUrl);
        notification = notificationRepository.save(notification);

        // Convert to DTO
        SystemNotificationDTO dto = new SystemNotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getTargetUrl(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );

        // Send via WebSocket to the user's specific topic
        // e.g. /topic/notifications/john_doe
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getUsername(), dto);

        return notification;
    }

    @Override
    public SystemNotification createNotification(String username, String title, String message, String type, String targetUrl) {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account != null) {
            return createNotification(account, title, message, type, targetUrl);
        }
        return null;
    }

    @Override
    public List<SystemNotification> getUnreadNotifications(String username) {
        return notificationRepository.findTop50ByRecipientAccount_UsernameAndIsReadFalseOrderByCreatedAtDesc(username);
    }

    @Override
    public Page<SystemNotification> getNotifications(String username, Pageable pageable) {
        return notificationRepository.findByRecipientAccount_UsernameOrderByCreatedAtDesc(username, pageable);
    }

    @Override
    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientAccount_UsernameAndIsReadFalse(username);
    }

    @Override
    public void markAsRead(Long notificationId, String username) {
        notificationRepository.markAsRead(notificationId, username);
    }

    @Override
    public void markAllAsRead(String username) {
        notificationRepository.markAllAsRead(username);
    }
}
