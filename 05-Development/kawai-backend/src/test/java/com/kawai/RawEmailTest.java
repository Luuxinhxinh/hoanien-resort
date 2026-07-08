package com.kawai;

import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class RawEmailTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void sendRawTemplates() throws Exception {
        String testEmail = "liungu2005@gmail.com";
        Path templatesDir = Paths.get("src/main/resources/templates/email");
        List<String> filesToSend = Arrays.asList("room-service.html", "refund-success.html", "invoice.html");

        for (String fileName : filesToSend) {
            try {
                Path p = templatesDir.resolve(fileName);
                String content = Files.readString(p);
                String subject = "[Preview Format FINAL] " + fileName;
                emailService.sendEmail(testEmail, subject, content);
                Thread.sleep(1000); // Prevent spam block
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Thread.sleep(5000); // wait for async
    }
}
