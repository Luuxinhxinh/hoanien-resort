package com.kawai.repositories;

import com.kawai.models.SystemNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    
    // Find all notifications for a specific account, ordered by creation date desc
    Page<SystemNotification> findByRecipientAccount_UsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    
    // Find top N unread notifications
    List<SystemNotification> findTop50ByRecipientAccount_UsernameAndIsReadFalseOrderByCreatedAtDesc(String username);
    
    // Count unread notifications
    long countByRecipientAccount_UsernameAndIsReadFalse(String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE SystemNotification n SET n.isRead = true WHERE n.id = :id AND n.recipientAccount.username = :username")
    void markAsRead(Long id, String username);

    @Modifying
    @Transactional
    @Query("UPDATE SystemNotification n SET n.isRead = true WHERE n.recipientAccount.username = :username")
    void markAllAsRead(String username);
}
