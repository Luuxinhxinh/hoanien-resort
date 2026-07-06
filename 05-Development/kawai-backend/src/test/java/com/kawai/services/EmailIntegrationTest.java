package com.kawai.services;

import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        System.out.println("====== STARTING EMAIL SENDING TEST ======");
        try {
            emailService.sendPasswordResetEmail("liungu2005@gmail.com", "https://kawai.com/reset?token=test-1234", "Nguyễn Văn Test");
            System.out.println("====== EMAIL TEST CALL FINISHED ======");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
