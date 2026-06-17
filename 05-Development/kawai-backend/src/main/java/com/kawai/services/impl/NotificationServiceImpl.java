package com.kawai.services.impl;

import com.kawai.services.interfaces.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void sendNotification(Long customerId, String title, String message) {
        // Dummy implementation
    }
}
