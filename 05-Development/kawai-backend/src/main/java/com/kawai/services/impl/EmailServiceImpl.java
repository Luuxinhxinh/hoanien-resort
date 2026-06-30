package com.kawai.services.impl;

import com.kawai.models.ConsolidatedInvoice;
import com.kawai.services.interfaces.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final java.time.format.DateTimeFormatter DATE_FMT = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy");
    private static final java.text.NumberFormat VND_FMT = java.text.NumberFormat
            .getInstance(new java.util.Locale("vi", "VN"));

    @org.springframework.beans.factory.annotation.Autowired
    private org.thymeleaf.TemplateEngine templateEngine;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:hoanien.00@gmail.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.resort-phone:1900 xxxx}")
    private String resortPhone;

    @Value("${app.mail.resort-website:https://hoaniensorretreat.vn}")
    private String resortWebsite;

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendBookingConfirmation(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer) {
        sendBookingConfirmation(booking, customer, "counter", "full", null);
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendBookingConfirmation(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer,
            boolean postToRoom, String roomDetail) {
        String paymentMethod = postToRoom ? "post-room" : "counter";
        String paymentType = postToRoom ? "room" : "full";
        if (booking.getNotes() != null && booking.getNotes().contains(";")) {
            java.util.Map<String, String> noteMap = new java.util.HashMap<>();
            String[] pairs = booking.getNotes().split(";");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    noteMap.put(kv[0], kv[1]);
                }
            }
            if (noteMap.containsKey("paymentMethod"))
                paymentMethod = noteMap.get("paymentMethod");
            if (noteMap.containsKey("paymentType"))
                paymentType = noteMap.get("paymentType");
        }
        sendBookingConfirmation(booking, customer, paymentMethod, paymentType, roomDetail);
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendBookingConfirmation(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer,
            String paymentMethod, String paymentType, String roomDetail) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email xác nhận: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }

        try {
            String tourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName()
                    : "Tour";
            String departureDate = booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                    ? booking.getSchedule().getDepartureDate().format(DATE_FMT)
                    : java.time.LocalDate.now().format(DATE_FMT);
            String departureTime = booking.getSchedule() != null && booking.getSchedule().getDepartureTime() != null
                    ? booking.getSchedule().getDepartureTime().toString().substring(0, 5)
                    : "--:--";

            // Parse metadata from notes
            int adultCount = booking.getParticipantCount() != null ? booking.getParticipantCount() : 1;
            int childCount = 0;
            java.math.BigDecimal childDiscountVal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal promoDiscountVal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal originalPriceVal = booking.getTotalPrice() != null ? booking.getTotalPrice()
                    : java.math.BigDecimal.ZERO;

            // If notes exist and are structured, parse them
            if (booking.getNotes() != null && booking.getNotes().contains(";")) {
                java.util.Map<String, String> noteMap = new java.util.HashMap<>();
                String[] pairs = booking.getNotes().split(";");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        noteMap.put(kv[0], kv[1]);
                    }
                }
                try {
                    if (noteMap.containsKey("adults"))
                        adultCount = Integer.parseInt(noteMap.get("adults"));
                    if (noteMap.containsKey("children"))
                        childCount = Integer.parseInt(noteMap.get("children"));
                    if (noteMap.containsKey("childDiscount"))
                        childDiscountVal = new java.math.BigDecimal(noteMap.get("childDiscount"));
                    if (noteMap.containsKey("promoDiscount"))
                        promoDiscountVal = new java.math.BigDecimal(noteMap.get("promoDiscount"));
                    if (noteMap.containsKey("originalPrice"))
                        originalPriceVal = new java.math.BigDecimal(noteMap.get("originalPrice"));
                    if (noteMap.containsKey("paymentMethod"))
                        paymentMethod = noteMap.get("paymentMethod");
                    if (noteMap.containsKey("paymentType"))
                        paymentType = noteMap.get("paymentType");
                } catch (Exception parseEx) {
                    logger.warn("Lỗi phân tích notes metadata cho booking {}: {}", booking.getId(),
                            parseEx.getMessage());
                }
            }

            // Determine if the customer is checked in for room gán nợ
            boolean isCheckedIn = false;
            if ("post-room".equalsIgnoreCase(paymentMethod)) {
                com.kawai.models.RoomBookingDetail detail = booking.getRoomBookingDetail();
                if (detail != null && detail.getRoomBooking() != null) {
                    String bookingStatus = detail.getRoomBooking().getBookingStatus();
                    String roomNumber = detail.getRoom() != null ? detail.getRoom().getRoomNumber() : "";
                    if ("Checked_In".equalsIgnoreCase(bookingStatus) && roomNumber != null
                            && !roomNumber.toUpperCase().startsWith("VIRTUAL_")) {
                        isCheckedIn = true;
                    }
                }
            }

            // Set up dynamic payment label & paymentAmount
            String paymentLabel = "Số tiền thanh toán";
            java.math.BigDecimal finalPrice = booking.getTotalPrice() != null ? booking.getTotalPrice()
                    : java.math.BigDecimal.ZERO;
            java.math.BigDecimal paymentAmountVal = finalPrice;
            if ("deposit".equalsIgnoreCase(paymentType)) {
                paymentLabel = "Số tiền đặt cọc (30%)";
                paymentAmountVal = finalPrice.multiply(new java.math.BigDecimal("0.3")).setScale(0,
                        java.math.RoundingMode.HALF_UP);
            } else if ("full".equalsIgnoreCase(paymentType)) {
                paymentLabel = "Tổng tiền thanh toán (100%)";
                paymentAmountVal = finalPrice;
            } else if ("room".equalsIgnoreCase(paymentType)) {
                paymentLabel = "Số tiền gán nợ phòng";
                paymentAmountVal = finalPrice;
            }

            String customerNotes = "";
            if (booking.getNotes() != null && booking.getNotes().contains("customerNotes=")) {
                int idx = booking.getNotes().indexOf("customerNotes=");
                customerNotes = booking.getNotes().substring(idx + "customerNotes=".length()).trim();
            }

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("tourName", tourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("departureTime", departureTime);
            ctx.setVariable("participantCount", booking.getParticipantCount());
            ctx.setVariable("bookingDate", java.time.LocalDate.now().format(DATE_FMT));
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            // Detailed invoice vars
            ctx.setVariable("basePrice",
                    formatVnd(booking.getSchedule() != null && booking.getSchedule().getTour() != null
                            ? booking.getSchedule().getTour().getBasePrice()
                            : java.math.BigDecimal.ZERO));
            ctx.setVariable("adultCount", adultCount);
            ctx.setVariable("childCount", childCount);
            ctx.setVariable("childDiscount", formatVnd(childDiscountVal));
            ctx.setVariable("childDiscountVal", childDiscountVal);
            ctx.setVariable("promoDiscount", formatVnd(promoDiscountVal));
            ctx.setVariable("promoDiscountVal", promoDiscountVal);
            ctx.setVariable("totalPrice", formatVnd(finalPrice));

            // Payment context
            ctx.setVariable("paymentMethod", paymentMethod);
            ctx.setVariable("paymentType", paymentType);
            ctx.setVariable("isCheckedIn", isCheckedIn);
            ctx.setVariable("roomDetail", roomDetail);
            ctx.setVariable("paymentLabel", paymentLabel);
            ctx.setVariable("paymentAmount", formatVnd(paymentAmountVal));
            ctx.setVariable("customerNotes", customerNotes);

            String html = templateEngine.process("email/tour-booking-confirmation", ctx);
            sendEmail(customer.getEmail(), "Xác nhận đặt tour - " + tourName + " | Hòa Niên Retreat & Resort", html);
            logger.info("Gửi email xác nhận đặt tour thành công (Nâng cao) → {} (booking #{})", customer.getEmail(),
                    booking.getId());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email xác nhận đặt tour nâng cao cho booking #{}: {}", booking.getId(),
                    e.getMessage(), e);
        }
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendCancellationNotice(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer,
            java.math.BigDecimal refundAmount, boolean cancelledByResort) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email hủy tour: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }

        try {
            String tourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName()
                    : "Tour";
            String departureDate = booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                    ? booking.getSchedule().getDepartureDate().format(DATE_FMT)
                    : "--/--/----";

            java.math.BigDecimal forfeitAmount = booking.getTotalPrice() != null && refundAmount != null
                    ? booking.getTotalPrice().subtract(refundAmount)
                    : java.math.BigDecimal.ZERO;

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("tourName", tourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("participantCount", booking.getParticipantCount());
            ctx.setVariable("totalPrice", formatVnd(booking.getTotalPrice()));
            ctx.setVariable("refundAmount", formatVnd(refundAmount));
            ctx.setVariable("forfeitAmount", formatVnd(forfeitAmount));
            ctx.setVariable("cancelledByResort", cancelledByResort);
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);
            ctx.setVariable("bgUrl", "https://i.ibb.co/99JSj0SF/BREmail.png");

            String html = templateEngine.process("email/tour-booking-cancelled", ctx);
            String subject = cancelledByResort ? "❌ Thông báo hủy tour — " + tourName + " | Hòa Niên Retreat & Resort"
                    : "❌ Xác nhận hủy tour — " + tourName + " | Hòa Niên Retreat & Resort";

            sendEmail(customer.getEmail(), subject, html);
            logger.info("Gửi email hủy tour thành công → {} (booking #{}, hoàn {})", customer.getEmail(),
                    booking.getId(), formatVnd(refundAmount));
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email hủy tour cho booking #{}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    private String formatVnd(java.math.BigDecimal amount) {
        if (amount == null)
            return "0 ₫";
        return VND_FMT.format(amount) + " ₫";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository;

    @Override
    public void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath) {
        if (!"Paid".equalsIgnoreCase(invoice.getInvoiceStatus())) {
            throw new IllegalStateException(
                    "Không thể gửi Email cho hóa đơn chưa được thanh toán (Trạng thái hiện tại: "
                            + invoice.getInvoiceStatus() + ")");
        }

        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));

            // Set basic info
            ctx.setVariable("customerName",
                    invoice.getBooking() != null && invoice.getBooking().getCustomer() != null
                            ? invoice.getBooking().getCustomer().getFullName()
                            : "Khách hàng");
            ctx.setVariable("invoiceNumber", invoice.getInvoiceNumber());
            ctx.setVariable("issuedDate", invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DATE_FMT)
                    : java.time.LocalDate.now().format(DATE_FMT));

            // Collect items from booking details
            java.util.List<java.util.Map<String, String>> items = new java.util.ArrayList<>();
            String roomNumber = "";
            java.math.BigDecimal depositAmount = java.math.BigDecimal.ZERO;
            String paymentMethod = "Chuyển khoản / Tiền mặt";

            if (invoice.getBooking() instanceof com.kawai.models.RoomBooking rb) {
                if (rb.getDepositAmount() != null) {
                    depositAmount = rb.getDepositAmount();
                }

                java.util.List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepository.findAll()
                        .stream()
                        .filter(d -> d.getRoomBooking() != null && d.getRoomBooking().getId().equals(rb.getId()))
                        .toList();

                if (!details.isEmpty()) {
                    for (com.kawai.models.RoomBookingDetail detail : details) {
                        if (detail.getRoom() != null) {
                            roomNumber += detail.getRoom().getRoomNumber() + " ";
                        }
                        java.util.Map<String, String> item = new java.util.HashMap<>();
                        item.put("date", rb.getCheckInDate() != null ? rb.getCheckInDate().format(DATE_FMT) : "");
                        item.put("description", "Tiền phòng ("
                                + (detail.getCategory() != null ? detail.getCategory().getCategoryName() : "Standard")
                                + ")");
                        item.put("amount", formatVnd(detail.getRoomCharge()));
                        items.add(item);
                    }
                }
            }

            ctx.setVariable("roomNumber", roomNumber.trim());
            ctx.setVariable("items", items);
            ctx.setVariable("paymentMethod", paymentMethod);
            ctx.setVariable("subtotal", formatVnd(invoice.getSubtotalBeforeVat()));
            ctx.setVariable("vatAmount", formatVnd(invoice.getVatAmount()));
            ctx.setVariable("depositAmount", formatVnd(depositAmount));
            ctx.setVariable("totalAmount", formatVnd(invoice.getTotalAmount().subtract(depositAmount)));

            String html = templateEngine.process("email/invoice", ctx);
            sendEmail(toEmail, "Hóa đơn thanh toán - " + invoice.getInvoiceNumber() + " | Hòa Niên Retreat & Resort",
                    html);
            logger.info("Gửi email hóa đơn thành công → {} (invoice #{})", toEmail, invoice.getInvoiceNumber());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email hóa đơn {}: {}", invoice.getInvoiceNumber(), e.getMessage(), e);
        }
    }

    @Override
    public void sendRegistrationOtpEmail(String toEmail, String otpCode, String fullName) {
        String subject = "Xác nhận đăng ký tài khoản - HOANIEN Retreat & Resort";
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
        ctx.setVariable("customerName", fullName);
        ctx.setVariable("otpCode", otpCode);
        ctx.setVariable("resortPhone", resortPhone);
        ctx.setVariable("resortWebsite", resortWebsite);
        String content = templateEngine.process("email/registration-otp", ctx);
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, String fullName) {
        String subject = "Đặt lại mật khẩu - HOANIEN Retreat & Resort";
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
        ctx.setVariable("customerName", fullName);
        ctx.setVariable("otpCode", resetLink);
        ctx.setVariable("resortPhone", resortPhone);
        ctx.setVariable("resortWebsite", resortWebsite);
        String content = templateEngine.process("email/password-reset", ctx);
        sendEmail(toEmail, subject, content);
    }

    public void sendEmail(String toEmail, String subject, String htmlContent) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Email không được gửi.");
            logger.info("[SENDGRID MOCK] To: {}, Subject: {}", toEmail, subject);
            return;
        }

        try {
            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            logger.info("[SENDGRID] Email sent to {} - Status: {}", toEmail, response.getStatusCode());
        } catch (IOException e) {
            logger.error("[SENDGRID] Lỗi gửi email tới {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendCustomWorkflowEmail(String customFromEmail, String toEmail, String subject, String htmlContent) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Custom Workflow Email không được gửi.");
            logger.info("[SENDGRID MOCK] From: {}, To: {}, Subject: {}", customFromEmail, toEmail, subject);
            logger.info("[SENDGRID MOCK CONTENT]: \n{}", htmlContent);
            return;
        }

        try {
            String finalFrom = (customFromEmail != null && !customFromEmail.trim().isEmpty()) ? customFromEmail
                    : fromEmail;
            Email from = new Email(finalFrom);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            logger.info("[SENDGRID] Custom Workflow Email sent from {} to {} - Status: {}", finalFrom, toEmail,
                    response.getStatusCode());
        } catch (IOException e) {
            logger.error("[SENDGRID] Lỗi gửi custom workflow email tới {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendSlaWarningEmail(String toEmail, String taskName, int pendingMinutes, String roomNumber) {
        String subject = "[SLA Warning] Nhiệm vụ chưa nhận việc quá hạn - HOANIEN Resort";
        String htmlContent = "<!DOCTYPE html><html><body style=\"font-family: 'Times New Roman', Times, serif;\">" +
                "<h2 style=\"color: #b73e3e;\">CẢNH BÁO SLA QUÁ HẠN NHẬN VIỆC</h2>" +
                "<p>Kính gửi Supervisor,</p>" +
                "<p>Hệ thống phát hiện nhiệm vụ sau đã quá hạn thời gian nhận việc theo quy định:</p>" +
                "<ul>" +
                "<li><strong>Nhiệm vụ:</strong> " + taskName + "</li>" +
                "<li><strong>Phòng:</strong> " + (roomNumber != null ? roomNumber : "N/A") + "</li>" +
                "<li><strong>Thời gian chờ:</strong> " + pendingMinutes + " phút (Vượt ngưỡng quy định)</li>" +
                "</ul>" +
                "<p>Vui lòng đăng nhập hệ thống Admin để điều phối và xử lý ngay lập tức.</p>" +
                "<br/><p>Trân trọng,<br/><i>HOANIEN Operational Workflow Engine</i></p>" +
                "</body></html>";
        sendEmail(toEmail, subject, htmlContent);
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendRoomServiceConfirmation(com.kawai.models.FoodOrder order, com.kawai.models.Customer customer,
            String roomNumber) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank())
            return;
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("orderId", order.getId());
            ctx.setVariable("roomNumber", roomNumber);
            ctx.setVariable("totalAmount", formatVnd(order.getTotalAmount()));
            ctx.setVariable("paymentMethod",
                    "CHARGE_TO_ROOM".equals(order.getPaymentType()) ? "Ghi nợ vào phòng" : "Thanh toán ngay");

            String itemsStr = "";
            if (order.getDetails() != null) {
                itemsStr = order.getDetails().stream()
                        .map(i -> (i.getMenuItem() != null ? i.getMenuItem().getItemName() : "Món") + " x"
                                + i.getQuantity())
                        .collect(java.util.stream.Collectors.joining(", "));
            }
            ctx.setVariable("items", itemsStr.isEmpty() ? "Không có" : itemsStr);

            String html = templateEngine.process("email/room-service", ctx);
            sendEmail(customer.getEmail(), "Xác nhận đơn phục vụ tại phòng #" + order.getId(), html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email Room Service cho order #{}: {}", order.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendTableBookingConfirmation(com.kawai.models.TableReservation reservation,
            com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank())
            return;
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("reservationId", reservation.getId());
            ctx.setVariable("reservationDate",
                    reservation.getReserveDate() != null ? reservation.getReserveDate().format(DATE_FMT) : "");
            ctx.setVariable("startTime",
                    reservation.getReserveTime() != null ? reservation.getReserveTime().toString() : "");
            ctx.setVariable("endTime", reservation.getEndTime() != null ? reservation.getEndTime().toString() : "");
            ctx.setVariable("tableNumber",
                    reservation.getTable() != null ? reservation.getTable().getTableNumber() : "Chưa xếp");
            ctx.setVariable("guestCount", reservation.getPartySize());
            ctx.setVariable("note",
                    reservation.getSpecialRequests() != null && !reservation.getSpecialRequests().isBlank()
                            ? reservation.getSpecialRequests()
                            : "Không có");

            String html = templateEngine.process("email/booking-table", ctx);
            sendEmail(customer.getEmail(), "Xác nhận đặt bàn thành công #" + reservation.getId(), html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email xác nhận đặt bàn #{}: {}", reservation.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendExtendTableHold(com.kawai.models.TableReservation reservation, com.kawai.models.Customer customer,
            int extendMinutes, String latestCheckInTime) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank())
            return;
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("reservationId", reservation.getId());
            ctx.setVariable("extendMinutes", extendMinutes);
            ctx.setVariable("latestCheckInTime", latestCheckInTime);

            String html = templateEngine.process("email/extend-hold", ctx);
            sendEmail(customer.getEmail(), "Gia hạn giữ bàn thành công #" + reservation.getId(), html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email gia hạn giữ bàn #{}: {}", reservation.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendCancelTableBooking(com.kawai.models.TableReservation reservation,
            com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank())
            return;
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("reservationId", reservation.getId());
            ctx.setVariable("reservationTime",
                    reservation.getReserveTime() != null ? reservation.getReserveTime().toString() : "");
            ctx.setVariable("reservationDate",
                    reservation.getReserveDate() != null ? reservation.getReserveDate().format(DATE_FMT) : "");

            String html = templateEngine.process("email/cancel-booking", ctx);
            sendEmail(customer.getEmail(), "Thông báo hủy đặt bàn #" + reservation.getId(), html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email hủy đặt bàn #{}: {}", reservation.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendRefundSuccessEmail(com.kawai.models.RefundRequest refundRequest, com.kawai.models.Customer customer,
            String absoluteAttachmentPath) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank())
            return;
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("orderId", refundRequest.getReferenceCode());
            ctx.setVariable("refundTime", refundRequest.getCompletedAt() != null ? refundRequest.getCompletedAt()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
            ctx.setVariable("bankName", refundRequest.getBankName());
            ctx.setVariable("accountNumber", refundRequest.getAccountNumber());
            ctx.setVariable("refundAmount", VND_FMT.format(refundRequest.getAmount()));

            String html = templateEngine.process("email/refund-success", ctx);

            Email from = new Email(fromEmail, "HOANIEN Resort");
            Email to = new Email(customer.getEmail());
            Content content = new Content("text/html", html);
            Mail mail = new Mail(from, "[HOANIEN] Xác nhận hoàn tiền thành công", to, content);

            if (absoluteAttachmentPath != null && !absoluteAttachmentPath.isBlank()) {
                java.io.File file = new java.io.File(absoluteAttachmentPath);
                if (file.exists()) {
                    byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
                    com.sendgrid.helpers.mail.objects.Attachments attachments = new com.sendgrid.helpers.mail.objects.Attachments();
                    attachments.setContent(java.util.Base64.getEncoder().encodeToString(fileData));
                    String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    String mimeType = "image/jpeg";
                    if (extension.equals("png"))
                        mimeType = "image/png";
                    attachments.setType(mimeType);
                    attachments.setFilename(file.getName());
                    attachments.setDisposition("attachment");
                    mail.addAttachments(attachments);
                }
            }

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info("Sent refund email to {}: status {}", customer.getEmail(), response.getStatusCode());
        } catch (Exception e) {
            logger.error("Lỗi gửi email hoàn tiền cho RefundRequest #{}: {}", refundRequest.getId(), e.getMessage());
        }
    }
}
