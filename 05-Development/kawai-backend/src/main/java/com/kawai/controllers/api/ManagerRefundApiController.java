package com.kawai.controllers.api;

import com.kawai.models.RefundRequest;
import com.kawai.models.FoodOrder;
import com.kawai.repositories.RefundRequestRepository;
import com.kawai.services.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager/refunds")
@PreAuthorize("hasAnyAuthority('OP_ANALYTICS', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class ManagerRefundApiController {

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeRefund(@PathVariable Long id, @RequestParam("billImage") MultipartFile file) {
        try {
            Optional<RefundRequest> optReq = refundRequestRepository.findById(id);
            if (optReq.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy yêu cầu hoàn tiền"));
            }
            
            RefundRequest req = optReq.get();
            if ("COMPLETED".equalsIgnoreCase(req.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu này đã được xử lý hoàn tiền trước đó."));
            }

            // Xử lý lưu file
            String uploadDir = "uploads/refund-bills/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                                : ".jpg";
            String newFilename = "bill_refund_" + id + "_" + UUID.randomUUID().toString() + extension;
            File destFile = new File(dir.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(destFile);

            // Cập nhật CSDL
            req.setEvidenceImageUrl("/uploads/refund-bills/" + newFilename);
            req.setStatus("COMPLETED");
            req.setCompletedAt(LocalDateTime.now());
            refundRequestRepository.save(req);

            // Gửi email cho khách (nếu có thông tin)
            FoodOrder order = req.getOrder();
            String customerEmail = null;
            com.kawai.models.Customer customer = null;
            if (order.getBooking() != null && order.getBooking().getCustomer() != null) {
                customer = order.getBooking().getCustomer();
                customerEmail = customer.getEmail();
            } else if (order.getRoomBookingDetail() != null 
                    && order.getRoomBookingDetail().getRoomBooking() != null 
                    && order.getRoomBookingDetail().getRoomBooking().getCustomer() != null) {
                customer = order.getRoomBookingDetail().getRoomBooking().getCustomer();
                customerEmail = customer.getEmail();
            }

            if (customerEmail != null && !customerEmail.isBlank() && customer != null) {
                try {
                    emailService.sendRefundSuccessEmail(req, customer, destFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Lỗi gửi email hoàn tiền: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of("message", "Thành công", "imageUrl", req.getEvidenceImageUrl()));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi lưu file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
