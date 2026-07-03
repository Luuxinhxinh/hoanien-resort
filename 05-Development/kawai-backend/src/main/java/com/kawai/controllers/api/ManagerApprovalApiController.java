package com.kawai.controllers.api;

import com.kawai.models.HotelOperation;
import com.kawai.models.Booking;
import com.kawai.models.Customer;
import com.kawai.repositories.HousekeepingTaskRepository;
import com.kawai.repositories.BookingRepository;
import com.kawai.services.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/manager/api/approvals")
@PreAuthorize("hasAnyAuthority('OP_ANALYTICS', 'ROLE_ADMIN', 'ROLE_MANAGER')")
public class ManagerApprovalApiController {

    @Autowired
    private HousekeepingTaskRepository housekeepingTaskRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/{taskId}/approve")
    public ResponseEntity<?> approveTask(@PathVariable Long taskId) {
        try {
            Optional<HotelOperation> optOp = housekeepingTaskRepository.findById(taskId);
            if (optOp.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy yêu cầu phê duyệt"));
            }

            HotelOperation op = optOp.get();
            java.util.List<String> validTypes = java.util.Arrays.asList("Manager_Approval", "Late_Checkout_Waiver", "Cancellation_Fee_Waiver", "Room_Downgrade_Refund");
            if (!validTypes.contains(op.getOperationalType())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nhiệm vụ không đúng loại phê duyệt"));
            }

            if (!"Pending".equalsIgnoreCase(op.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu đã được xử lý trước đó."));
            }

            // Parse bookingId
            Long bookingId = parseBookingIdFromNotes(op.getNotes());
            if (bookingId != null) {
                Optional<Booking> optBk = bookingRepository.findById(bookingId);
                if (optBk.isPresent()) {
                    Booking bk = optBk.get();
                    
                    // Xử lý hậu kỳ theo loại yêu cầu
                    if ("Manager_Approval".equals(op.getOperationalType())) {
                        bk.setBookingStatus("Confirmed");
                        bookingRepository.save(bk);
                        
                        // Gửi email thông báo cho khách hàng
                        Customer customer = bk.getCustomer();
                        if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                            try {
                                String subject = "Đơn đặt phòng của bạn đã được phê duyệt hạn mức khuyến mãi";
                                String content = "<h3>Xin chào " + customer.getFullName() + ",</h3>" +
                                        "<p>Yêu cầu phê duyệt mã giảm giá vượt hạn mức cho đơn hàng <strong>BK-" + bk.getId() + "</strong> của bạn đã được quản lý chấp nhận.</p>" +
                                        "<p>Vui lòng tiến hành thanh toán hoặc xác nhận đơn hàng để hoàn tất đặt phòng.</p>" +
                                        "<p>Xin cảm ơn!</p>";
                                emailService.sendEmail(customer.getEmail(), subject, content);
                            } catch (Exception e) {
                                System.err.println("Lỗi gửi email duyệt khuyến mãi: " + e.getMessage());
                            }
                        }
                    } else if ("Cancellation_Fee_Waiver".equals(op.getOperationalType())) {
                        bk.setBookingStatus("CANCELLED");
                        bookingRepository.save(bk);
                        
                        // Gửi email thông báo miễn phí phạt hủy
                        Customer customer = bk.getCustomer();
                        if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                            try {
                                String subject = "Yêu cầu miễn phí phạt hủy của bạn đã được phê duyệt";
                                String content = "<h3>Xin chào " + customer.getFullName() + ",</h3>" +
                                        "<p>Yêu cầu miễn phí phạt hủy cho đơn hàng <strong>BK-" + bk.getId() + "</strong> của bạn đã được quản lý phê duyệt thành công.</p>" +
                                        "<p>Bạn sẽ không phải thanh toán phí phạt hủy phòng cho đơn này.</p>" +
                                        "<p>Trân trọng cảm ơn!</p>";
                                emailService.sendEmail(customer.getEmail(), subject, content);
                            } catch (Exception e) {
                                System.err.println("Lỗi gửi email duyệt miễn phạt: " + e.getMessage());
                            }
                        }
                    } else if ("Late_Checkout_Waiver".equals(op.getOperationalType())) {
                        // Miễn phí trả phòng trễ: ghi nhận vào notes hoặc xử lý trong folio. 
                        // Không cần thay đổi trạng thái booking.
                    } else if ("Room_Downgrade_Refund".equals(op.getOperationalType())) {
                        // Hoàn tiền đổi phòng: đánh dấu tác vụ hoàn tất, tiền sẽ được hoàn trả ở folio.
                    }
                }
            }

            op.setStatus("Completed");
            op.setCompletedAt(LocalDateTime.now());
            housekeepingTaskRepository.save(op);

            return ResponseEntity.ok(Map.of("message", "Đã phê duyệt thành công", "status", "Completed"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/{taskId}/reject")
    public ResponseEntity<?> rejectTask(@PathVariable Long taskId) {
        try {
            Optional<HotelOperation> optOp = housekeepingTaskRepository.findById(taskId);
            if (optOp.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy yêu cầu phê duyệt"));
            }

            HotelOperation op = optOp.get();
            java.util.List<String> validTypes = java.util.Arrays.asList("Manager_Approval", "Late_Checkout_Waiver", "Cancellation_Fee_Waiver", "Room_Downgrade_Refund");
            if (!validTypes.contains(op.getOperationalType())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nhiệm vụ không đúng loại phê duyệt"));
            }

            if (!"Pending".equalsIgnoreCase(op.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu đã được xử lý trước đó."));
            }

            // Parse bookingId
            Long bookingId = parseBookingIdFromNotes(op.getNotes());
            if (bookingId != null) {
                Optional<Booking> optBk = bookingRepository.findById(bookingId);
                if (optBk.isPresent()) {
                    Booking bk = optBk.get();
                    
                    // Chỉ hủy booking đối với yêu cầu duyệt khuyến mãi vượt hạn mức khi bị từ chối
                    if ("Manager_Approval".equals(op.getOperationalType())) {
                        bk.setBookingStatus("CANCELLED");
                        bookingRepository.save(bk);
                        
                        // Gửi email cho khách hàng
                        Customer customer = bk.getCustomer();
                        if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                            try {
                                String subject = "Đơn đặt phòng của bạn đã bị từ chối phê duyệt khuyến mãi";
                                String content = "<h3>Xin chào " + customer.getFullName() + ",</h3>" +
                                        "<p>Yêu cầu phê duyệt mã giảm giá vượt hạn mức cho đơn hàng <strong>BK-" + bk.getId() + "</strong> của bạn đã bị quản lý từ chối.</p>" +
                                        "<p>Đơn đặt phòng đã tự động hủy. Vui lòng tạo đặt phòng mới với mã giảm giá hợp lệ hoặc liên hệ khách sạn để biết thêm chi tiết.</p>" +
                                        "<p>Trân trọng cảm ơn!</p>";
                                emailService.sendEmail(customer.getEmail(), subject, content);
                            } catch (Exception e) {
                                System.err.println("Lỗi gửi email từ chối duyệt: " + e.getMessage());
                            }
                        }
                    } else if ("Cancellation_Fee_Waiver".equals(op.getOperationalType())) {
                        // Từ chối miễn phí phạt hủy: Booking vẫn giữ trạng thái cũ (Confirmed/Checked_Out), khách vẫn phải đóng phạt.
                    } else if ("Late_Checkout_Waiver".equals(op.getOperationalType())) {
                        // Từ chối miễn phí trễ: Khách vẫn phải trả tiền phụ thu trễ lúc check-out.
                    } else if ("Room_Downgrade_Refund".equals(op.getOperationalType())) {
                        // Từ chối hoàn tiền đổi phòng: Không được hoàn tiền.
                    }
                }
            }

            op.setStatus("Rejected");
            op.setCompletedAt(LocalDateTime.now());
            housekeepingTaskRepository.save(op);

            return ResponseEntity.ok(Map.of("message", "Đã từ chối phê duyệt thành công", "status", "Rejected"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    private Long parseBookingIdFromNotes(String notes) {
        if (notes == null) return null;
        int idx = notes.lastIndexOf("booking ID: ");
        if (idx == -1) {
            idx = notes.lastIndexOf("ID: ");
        }
        if (idx != -1) {
            try {
                String sub = notes.substring(idx + 12).trim();
                if (sub.isEmpty() || !Character.isDigit(sub.charAt(0))) {
                    sub = notes.substring(idx + 4).trim();
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < sub.length(); i++) {
                    char c = sub.charAt(i);
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                if (sb.length() > 0) {
                    return Long.parseLong(sb.toString());
                }
            } catch (Exception e) {
                // fallback
            }
        }
        return null;
    }
}
