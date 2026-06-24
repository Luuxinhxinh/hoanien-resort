package com.kawai.listeners;

import com.kawai.events.SystemEmailEvent;
import com.kawai.models.ConsolidatedInvoice;
import com.kawai.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemEmailEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleSystemEmailEvent(SystemEmailEvent event) {
        log.info("[SystemEmailEventListener] Bắt đầu gửi email (Async) tới: {}, Loại: {}", event.getToEmail(), event.getTemplateName());

        try {
            switch (event.getTemplateName()) {
                case "registration-otp":
                    emailService.sendRegistrationOtpEmail(
                            event.getToEmail(),
                            (String) event.getContext().get("otpCode"),
                            (String) event.getContext().get("fullName")
                    );
                    break;
                case "password-reset":
                    emailService.sendPasswordResetEmail(
                            event.getToEmail(),
                            (String) event.getContext().get("resetLink"),
                            (String) event.getContext().get("fullName")
                    );
                    break;
                case "invoice":
                    emailService.sendInvoiceEmail(
                            event.getToEmail(),
                            (ConsolidatedInvoice) event.getContext().get("invoice"),
                            (String) event.getContext().get("pdfPath")
                    );
                    break;
                case "sla-warning":
                    emailService.sendSlaWarningEmail(
                            event.getToEmail(),
                            (String) event.getContext().get("taskName"),
                            (Integer) event.getContext().get("pendingMinutes"),
                            (String) event.getContext().get("roomNumber")
                    );
                    break;
                case "custom-workflow":
                    emailService.sendCustomWorkflowEmail(
                            (String) event.getContext().get("fromEmail"),
                            event.getToEmail(),
                            event.getSubject(),
                            (String) event.getContext().get("htmlContent")
                    );
                    break;
                default:
                    log.warn("[SystemEmailEventListener] Không tìm thấy handler cho template: {}", event.getTemplateName());
                    break;
            }
            log.info("[SystemEmailEventListener] Gửi email thành công tới: {}", event.getToEmail());
        } catch (Exception e) {
            log.error("[SystemEmailEventListener] Lỗi khi gửi email tới {}: {}", event.getToEmail(), e.getMessage());
        }
    }
}
