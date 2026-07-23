
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
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final java.time.format.DateTimeFormatter DATE_FMT = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy");
    private static final java.text.NumberFormat VND_FMT = java.text.NumberFormat
            .getInstance(new java.util.Locale("vi", "VN"));

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Autowired
    private org.thymeleaf.TemplateEngine templateEngine;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourScheduleRepository tourScheduleRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourStaffAssignmentRepository tourStaffAssignmentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourAttendeeRepository tourAttendeeRepository;

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

            // Insurance context variables
            String insurancePolicyNumber = "Đang cập nhật (chờ cấp đơn)";
            boolean hasInsurance = false;
            if (booking.getSchedule() != null) {
                if (booking.getSchedule().getTour() != null
                        && Boolean.TRUE.equals(booking.getSchedule().getTour().getIsInsuranceRequired())) {
                    hasInsurance = true;
                    if (booking.getSchedule().getInsurancePolicyNumber() != null) {
                        insurancePolicyNumber = booking.getSchedule().getInsurancePolicyNumber();
                    }
                }
            }
            ctx.setVariable("hasInsurance", hasInsurance);
            ctx.setVariable("insurancePolicyNumber", insurancePolicyNumber);

            java.util.List<com.kawai.models.TourAttendee> attendeesList = tourAttendeeRepository
                    .findByTourBookingId(booking.getId());
            java.util.List<java.util.Map<String, String>> formattedAttendees = new java.util.ArrayList<>();
            if (attendeesList != null) {
                for (com.kawai.models.TourAttendee att : attendeesList) {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    String name = "";
                    String cccd = "";
                    if (att.getCustomer() != null) {
                        name = att.getCustomer().getFullName();
                        cccd = com.kawai.utils.EncryptionUtils.decrypt(att.getCustomer().getCccdPassportEncrypted());
                    } else if (att.getDependent() != null) {
                        name = att.getDependent().getDependentName();
                        cccd = com.kawai.utils.EncryptionUtils.decrypt(att.getDependent().getCccdPassportEncrypted());
                    }
                    map.put("name", name != null ? name.toUpperCase() : "");
                    map.put("cccd", cccd != null ? cccd : "");
                    formattedAttendees.add(map);
                }
            }
            ctx.setVariable("formattedAttendees", formattedAttendees);
            ctx.setVariable("attendees", attendeesList != null ? attendeesList : new java.util.ArrayList<>());

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
            java.math.BigDecimal refundAmount, boolean cancelledByResort, String reason) {
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
                    : java.time.LocalDate.now().format(DATE_FMT);
            String departureTime = booking.getSchedule() != null && booking.getSchedule().getDepartureTime() != null
                    ? booking.getSchedule().getDepartureTime().toString().substring(0, 5)
                    : "--:--";

            int adultCount = booking.getParticipantCount() != null ? booking.getParticipantCount() : 1;
            int childCount = 0;
            java.math.BigDecimal childDiscountVal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal promoDiscountVal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal originalPriceVal = booking.getTotalPrice() != null ? booking.getTotalPrice()
                    : java.math.BigDecimal.ZERO;

            String paymentMethod = "counter";
            String paymentType = "full";
            String roomDetail = "";

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
                    if (noteMap.containsKey("roomDetail"))
                        roomDetail = noteMap.get("roomDetail");
                } catch (Exception parseEx) {
                    logger.warn("Lỗi phân tích notes metadata cho booking {}: {}", booking.getId(),
                            parseEx.getMessage());
                }
            }

            if ("post-room".equalsIgnoreCase(paymentMethod)) {
                com.kawai.models.RoomBookingDetail detail = booking.getRoomBookingDetail();
                if (detail != null && detail.getRoom() != null) {
                    roomDetail = detail.getRoom().getRoomNumber();
                }
            }

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

            java.math.BigDecimal forfeitAmount = booking.getTotalPrice() != null && refundAmount != null
                    ? booking.getTotalPrice().subtract(refundAmount)
                    : java.math.BigDecimal.ZERO;

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("tourName", tourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("departureTime", departureTime);
            ctx.setVariable("participantCount", booking.getParticipantCount());
            ctx.setVariable("bookingDate", booking.getBookingDate() != null ? booking.getBookingDate().format(DATE_FMT)
                    : java.time.LocalDate.now().format(DATE_FMT));
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

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

            ctx.setVariable("paymentMethod", paymentMethod);
            ctx.setVariable("paymentType", paymentType);
            ctx.setVariable("isCheckedIn", isCheckedIn);
            ctx.setVariable("roomDetail", roomDetail);
            ctx.setVariable("paymentLabel", paymentLabel);
            ctx.setVariable("paymentAmount", formatVnd(paymentAmountVal));

            ctx.setVariable("refundAmount", formatVnd(refundAmount));
            ctx.setVariable("forfeitAmount", formatVnd(forfeitAmount));
            ctx.setVariable("cancelledByResort", cancelledByResort);
            ctx.setVariable("reason", reason);

            String refundMethodDetail = "Số tiền hoàn trả sẽ được chuyển trả vào phương thức thanh toán gốc của Quý khách. Vui lòng chờ 3-7 ngày làm việc để ngân hàng xử lý giao dịch.";
            if ("post-room".equalsIgnoreCase(paymentMethod)) {
                refundMethodDetail = "Số tiền hoàn đã được cộng (ghi âm) trực tiếp vào Folio phòng nghỉ số "
                        + roomDetail
                        + " của Quý khách. Số tiền này sẽ được trừ trực tiếp khi Quý khách làm thủ tục thanh toán checkout phòng nghỉ.";
            } else if ("vnpay".equalsIgnoreCase(paymentMethod)) {
                refundMethodDetail = "Số tiền hoàn trả sẽ được chuyển trả trực tiếp vào tài khoản ngân hàng / ví điện tử gốc Quý khách đã sử dụng thanh toán qua VNPay. Vui lòng chờ từ 3-7 ngày làm việc để ngân hàng xử lý.";
            } else if ("counter".equalsIgnoreCase(paymentMethod)) {
                refundMethodDetail = "Quý khách vui lòng mang theo hóa đơn hoặc thông tin đặt tour này liên hệ quầy Lễ tân của resort để nhận lại tiền mặt hoàn trả, hoặc phản hồi email này cung cấp thông tin số tài khoản để resort thực hiện chuyển khoản.";
            }
            ctx.setVariable("refundMethodDetail", refundMethodDetail);

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

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.FolioItemRepository folioItemRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.TourBookingRepository tourBookingRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.RoomCategoryRepository roomCategoryRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.FoodOrderRepository foodOrderRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.services.interfaces.PaymentService paymentService;

    @Override
    public void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath) {
        if (!"Paid".equalsIgnoreCase(invoice.getInvoiceStatus()) && !"Partial_Paid".equalsIgnoreCase(invoice.getInvoiceStatus())) {
            throw new IllegalStateException(
                    "Không thể gửi Email cho hóa đơn chưa được thanh toán (Trạng thái hiện tại: "
                            + invoice.getInvoiceStatus() + ")");
        }

        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));

            ctx.setVariable("customerName",
                    invoice.getBooking() != null && invoice.getBooking().getCustomer() != null
                            ? invoice.getBooking().getCustomer().getFullName()
                            : "Khách hàng");
            ctx.setVariable("invoiceNumber", invoice.getInvoiceNumber());
            
            // Collect items from booking details
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            String roomNumber = "";
            java.math.BigDecimal depositAmount = java.math.BigDecimal.ZERO;
            String paymentMethod = "Chuyển khoản / Tiền mặt";
            int guestCount = 0;
            String checkInDateStr = "N/A";
            String checkOutDateStr = "N/A";

            com.kawai.models.Booking baseBooking = invoice.getBooking();
            if (baseBooking instanceof org.hibernate.proxy.HibernateProxy proxy) {
                baseBooking = (com.kawai.models.Booking) proxy.getHibernateLazyInitializer().getImplementation();
            }

            if (baseBooking instanceof com.kawai.models.RoomBooking rb) {
                checkInDateStr = rb.getCheckInDate() != null ? rb.getCheckInDate().format(DATE_FMT) : "N/A";
                checkOutDateStr = rb.getCheckOutDate() != null ? rb.getCheckOutDate().format(DATE_FMT) : "N/A";

                // Retrieve all payment transactions to get deposit amount
                try {
                    java.util.List<com.kawai.models.PaymentTransaction> payments = paymentService.getPaymentsByBookingId(rb.getId());
                    if (payments != null) {
                        for (com.kawai.models.PaymentTransaction pt : payments) {
                            if (pt.getStatus() == com.kawai.models.PaymentStatus.SUCCESS && pt.getAmount() != null) {
                                if ("ROOM_BOOKING".equalsIgnoreCase(pt.getTransactionType())
                                        || "DEPOSIT".equalsIgnoreCase(pt.getTransactionType())) {
                                    depositAmount = depositAmount.add(pt.getAmount());
                                }
                                if (pt.getPaymentMethod() != null) {
                                    paymentMethod = pt.getPaymentMethod();
                                }
                            }
                        }
                    }
                } catch (Exception e) {}
                if (depositAmount.compareTo(java.math.BigDecimal.ZERO) == 0 && rb.getDepositAmount() != null) {
                    depositAmount = rb.getDepositAmount();
                }

                // Filter details by Checked_Out status (and map guest counts)
                java.util.List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(rb.getId())
                        .stream()
                        .filter(d -> "Checked_Out".equalsIgnoreCase(d.getDetailStatus()))
                        .toList();

                if (!details.isEmpty()) {
                    java.util.List<TempItem> groupedItems = new java.util.ArrayList<>();

                    for (com.kawai.models.RoomBookingDetail detail : details) {
                        if (detail.getRoom() != null) {
                            roomNumber += detail.getRoom().getRoomNumber() + " ";
                        }
                        if (detail.getNumberOfAdults() != null) guestCount += detail.getNumberOfAdults();
                        if (detail.getNumberOfChildren() != null) guestCount += detail.getNumberOfChildren();

                        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                                rb.getCheckInDate(),
                                rb.getCheckOutDate()
                        );
                        if (nights <= 0) nights = 1;

                        String originalCategoryName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "Room";
                        java.math.BigDecimal pricePerNight = detail.getRoomCharge().divide(java.math.BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP);
                        
                        if (detail.getCategory() != null && detail.getCategory().getBasePrice() != null 
                                && detail.getCategory().getBasePrice().compareTo(pricePerNight) != 0) {
                            originalCategoryName = roomCategoryRepository.findAll().stream()
                                    .filter(cat -> cat.getBasePrice() != null && cat.getBasePrice().compareTo(pricePerNight) == 0)
                                    .map(com.kawai.models.RoomCategory::getCategoryName)
                                    .findFirst()
                                    .orElse(originalCategoryName);
                        }

                        // Fetch FolioItems for this roomBookingDetailId
                        java.util.List<com.kawai.models.FolioItem> folioItems = folioItemRepository.findByRoomBookingDetailId(detail.getId());
                        if (folioItems == null) {
                            folioItems = new java.util.ArrayList<>();
                        }

                        // Check if a main Room Charge was already posted to FolioItem
                        boolean hasRoomCharge = folioItems.stream()
                                .anyMatch(fi -> "Room".equalsIgnoreCase(fi.getSourceDepartment()) 
                                        && fi.getDescription() != null 
                                        && fi.getDescription().contains("Room Charge"));

                        // If not, virtually add the expected base room charge
                        java.util.List<com.kawai.models.FolioItem> allItemsToProcess = new java.util.ArrayList<>(folioItems);
                        if (!hasRoomCharge) {
                            com.kawai.models.FolioItem virtualRoomCharge = new com.kawai.models.FolioItem();
                            virtualRoomCharge.setSourceDepartment("Room");
                            virtualRoomCharge.setAmount(detail.getRoomCharge());
                            virtualRoomCharge.setDescription("Room Charge - " + originalCategoryName);
                            virtualRoomCharge.setIsSettledSeparately(false);
                            virtualRoomCharge.setCreatedAt(rb.getCheckInDate().atStartOfDay());
                            allItemsToProcess.add(0, virtualRoomCharge); // add to top
                        }

                        // Group all items
                        for (com.kawai.models.FolioItem fi : allItemsToProcess) {
                            if (Boolean.TRUE.equals(fi.getIsSettledSeparately())) {
                                continue;
                            }

                            String dateStr = fi.getCreatedAt() != null ? fi.getCreatedAt().format(DATE_FMT) : "";

                            if ("Tour".equalsIgnoreCase(fi.getSourceDepartment()) && fi.getBooking() instanceof com.kawai.models.TourBooking tb) {
                                // Let's resolve participant details
                                int adultCount = 0;
                                int childCount = 0;
                                int infantCount = 0;

                                java.util.List<com.kawai.models.TourAttendee> attendees = tourAttendeeRepository.findByTourBookingId(tb.getId());
                                if (attendees != null && !attendees.isEmpty()) {
                                    for (com.kawai.models.TourAttendee attendee : attendees) {
                                        if (attendee.getCustomer() != null) {
                                            adultCount++;
                                        } else if (attendee.getDependent() != null) {
                                            com.kawai.models.Dependent dep = attendee.getDependent();
                                            String cccd = dep.getCccdPassportEncrypted();
                                            if (cccd != null && cccd.startsWith("AUTO_CHILD_")) {
                                                if (cccd.contains("Dưới_2_tuổi") || cccd.contains("Dưới 2 tuổi")) {
                                                    infantCount++;
                                                } else {
                                                    childCount++;
                                                }
                                            } else if (dep.getBirthDate() != null) {
                                                int age = java.time.Period.between(dep.getBirthDate(), java.time.LocalDate.now()).getYears();
                                                if (age < 2) {
                                                    infantCount++;
                                                } else if (age < 12) {
                                                    childCount++;
                                                } else {
                                                    adultCount++;
                                                }
                                            } else {
                                                childCount++;
                                            }
                                        } else {
                                            adultCount++;
                                        }
                                    }
                                } else {
                                    adultCount = tb.getParticipantCount() != null ? tb.getParticipantCount() : 0;
                                }

                                java.math.BigDecimal totalCharge = tb.getTourCharge() != null ? tb.getTourCharge() : java.math.BigDecimal.ZERO;
                                double weight = adultCount + 0.5 * childCount;
                                java.math.BigDecimal adultPrice = java.math.BigDecimal.ZERO;
                                java.math.BigDecimal childPrice = java.math.BigDecimal.ZERO;

                                com.kawai.models.TourSchedule sched = tb.getSchedule();
                                com.kawai.models.Tour tourObj = sched != null ? sched.getTour() : null;

                                if (weight > 0) {
                                    adultPrice = totalCharge.divide(java.math.BigDecimal.valueOf(weight), 2, java.math.RoundingMode.HALF_UP);
                                    childPrice = adultPrice.multiply(new java.math.BigDecimal("0.5")).setScale(2, java.math.RoundingMode.HALF_UP);
                                } else if (tourObj != null && tourObj.getBasePrice() != null) {
                                    adultPrice = tourObj.getBasePrice();
                                    childPrice = adultPrice.multiply(new java.math.BigDecimal("0.5")).setScale(2, java.math.RoundingMode.HALF_UP);
                                }

                                java.math.BigDecimal insuranceFee = java.math.BigDecimal.ZERO;
                                String notes = tb.getNotes();
                                if (notes != null) {
                                    String[] parts = notes.split(";");
                                    for (String part : parts) {
                                        if (part.startsWith("insuranceFee=")) {
                                            try {
                                                insuranceFee = new java.math.BigDecimal(part.substring("insuranceFee=".length()).trim());
                                            } catch (Exception e) {}
                                        }
                                    }
                                }

                                String insurancePolicyNumber = null;
                                if (sched != null && Boolean.TRUE.equals(sched.getIsInsuranceProcessed())) {
                                    insurancePolicyNumber = sched.getInsurancePolicyNumber();
                                }

                                int qty = tb.getParticipantCount() != null ? tb.getParticipantCount() : (adultCount + childCount + infantCount);
                                if (qty <= 0) qty = 1;
                                java.math.BigDecimal unitPrice = totalCharge.divide(java.math.BigDecimal.valueOf(qty), 2, java.math.RoundingMode.HALF_UP);

                                String departureTimeStr = sched != null && sched.getDepartureTime() != null ? sched.getDepartureTime().toString().substring(0, 5) : "";
                                String departureDateStr = sched != null && sched.getDepartureDate() != null ? sched.getDepartureDate().toString() : "";
                                String tourName = tourObj != null ? tourObj.getTourName() : "Tour";

                                TempItem tourParent = new TempItem();
                                tourParent.date = dateStr;
                                tourParent.description = "Tour Charge - " + tourName + " (Khởi hành: " + departureTimeStr + " - " + departureDateStr + ")";
                                tourParent.qty = qty;
                                tourParent.unitVal = unitPrice;
                                tourParent.amount = totalCharge;
                                tourParent.isParentTour = true;

                                if (adultCount > 0) {
                                    TempItem adultRow = new TempItem();
                                    adultRow.description = "- người lớn";
                                    adultRow.qty = adultCount;
                                    adultRow.unitVal = adultPrice;
                                    adultRow.amount = adultPrice.multiply(java.math.BigDecimal.valueOf(adultCount));
                                    adultRow.isChild = true;
                                    tourParent.children.add(adultRow);
                                }
                                if (childCount > 0) {
                                    TempItem childRow = new TempItem();
                                    childRow.description = "- trẻ em (2-11 tuổi)";
                                    childRow.qty = childCount;
                                    childRow.unitVal = childPrice;
                                    childRow.amount = childPrice.multiply(java.math.BigDecimal.valueOf(childCount));
                                    childRow.isChild = true;
                                    tourParent.children.add(childRow);
                                }
                                if (infantCount > 0) {
                                    TempItem infantRow = new TempItem();
                                    infantRow.description = "- em bé (dưới 2 tuổi)";
                                    infantRow.qty = infantCount;
                                    infantRow.unitVal = java.math.BigDecimal.ZERO;
                                    infantRow.amount = java.math.BigDecimal.ZERO;
                                    infantRow.isChild = true;
                                    tourParent.children.add(infantRow);
                                }
                                if (tourObj != null && (Boolean.TRUE.equals(tourObj.getIsInsuranceRequired()) || insuranceFee.compareTo(java.math.BigDecimal.ZERO) > 0)) {
                                    String policySuffix = insurancePolicyNumber != null ? " (" + insurancePolicyNumber + ")" : "";
                                    TempItem insRow = new TempItem();
                                    insRow.description = "- bảo hiểm du lịch bắt buộc" + policySuffix;
                                    insRow.qty = qty;
                                    insRow.unitVal = tourObj.getInsurancePrice() != null ? tourObj.getInsurancePrice() : new java.math.BigDecimal("50000");
                                    insRow.amount = java.math.BigDecimal.ZERO; // "Đã bao gồm"
                                    insRow.isChild = true;
                                    tourParent.children.add(insRow);
                                }
                                groupedItems.add(tourParent);

                            } else if ("F&B".equalsIgnoreCase(fi.getSourceDepartment()) || (fi.getDescription() != null && fi.getDescription().toUpperCase().contains("ORDER #"))) {
                                String desc = fi.getDescription();
                                Long orderId = null;
                                java.util.regex.Matcher m = java.util.regex.Pattern.compile("Order\\s*(?:#|số)?\\s*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(desc);
                                if (m.find()) {
                                    try {
                                        orderId = Long.parseLong(m.group(1));
                                    } catch (Exception e) {}
                                }

                                com.kawai.models.FoodOrder foodOrder = null;
                                if (orderId != null) {
                                    foodOrder = foodOrderRepository.findById(orderId).orElse(null);
                                }

                                if (foodOrder != null && foodOrder.getDetails() != null && !foodOrder.getDetails().isEmpty()) {
                                    java.math.BigDecimal dishesTotal = java.math.BigDecimal.ZERO;
                                    for (com.kawai.models.FoodOrderDetail od : foodOrder.getDetails()) {
                                        dishesTotal = dishesTotal.add(od.getTotalPrice());
                                    }

                                    TempItem fnbParent = new TempItem();
                                    fnbParent.date = dateStr;
                                    fnbParent.description = "F&B CHARGES (Order #" + orderId + ")";
                                    fnbParent.qty = 1;
                                    fnbParent.unitVal = dishesTotal;
                                    fnbParent.amount = dishesTotal;
                                    fnbParent.isParentFnb = true;

                                    for (com.kawai.models.FoodOrderDetail od : foodOrder.getDetails()) {
                                        TempItem dishRow = new TempItem();
                                        dishRow.description = "- " + (od.getMenuItem() != null ? od.getMenuItem().getItemName() : "Món ăn");
                                        dishRow.qty = od.getQuantity();
                                        dishRow.unitVal = od.getPriceAtOrder();
                                        dishRow.amount = od.getTotalPrice();
                                        dishRow.isChild = true;
                                        fnbParent.children.add(dishRow);
                                    }
                                    groupedItems.add(fnbParent);
                                } else {
                                    TempItem normalRow = new TempItem();
                                    normalRow.key = desc + "_" + fi.getAmount();
                                    normalRow.date = dateStr;
                                    normalRow.description = desc;
                                    normalRow.qty = 1;
                                    normalRow.unitVal = fi.getAmount();
                                    normalRow.amount = fi.getAmount();
                                    groupedItems.add(normalRow);
                                }
                            } else {
                                // Room or Others
                                String desc = fi.getDescription() != null ? fi.getDescription() : "";
                                desc = desc.replaceAll("(?i)\\s*\\(Expected\\)", "").replaceAll("(?i)\\s*Expected\\s*", "").trim();

                                boolean isMainRoomCharge = "Room".equalsIgnoreCase(fi.getSourceDepartment()) 
                                        && !desc.contains("surcharge") 
                                        && !desc.contains("phụ phí") 
                                        && !desc.contains("nâng hạng");

                                int itemQty = isMainRoomCharge ? (int) nights : 1;
                                java.math.BigDecimal itemUnitVal = isMainRoomCharge 
                                        ? fi.getAmount().divide(java.math.BigDecimal.valueOf(nights), 2, java.math.RoundingMode.HALF_UP)
                                        : fi.getAmount();

                                String key = desc + "_" + itemUnitVal;

                                TempItem existing = null;
                                for (TempItem g : groupedItems) {
                                    if (key.equals(g.key) && !g.isChild && !g.isParentTour && !g.isParentFnb && !g.isParentMinibar) {
                                        existing = g;
                                        break;
                                    }
                                }

                                if (existing != null) {
                                    existing.qty += itemQty;
                                    existing.amount = existing.amount.add(fi.getAmount());
                                } else {
                                    TempItem normalRow = new TempItem();
                                    normalRow.key = key;
                                    normalRow.date = dateStr;
                                    normalRow.description = desc;
                                    normalRow.qty = itemQty;
                                    normalRow.unitVal = itemUnitVal;
                                    normalRow.amount = fi.getAmount();
                                    groupedItems.add(normalRow);
                                }
                            }
                        }
                    }

                    // Sum up computed subtotal from non-child grouped items
                    java.math.BigDecimal computedSubtotal = java.math.BigDecimal.ZERO;
                    String lastDateStr = "";
                    for (TempItem g : groupedItems) {
                        computedSubtotal = computedSubtotal.add(g.amount);
                        
                        // Map grouped items into final variables list for template
                        String displayDate = g.date.equals(lastDateStr) ? "" : g.date;
                        if (!g.date.isEmpty()) {
                            lastDateStr = g.date;
                        }

                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        map.put("date", displayDate);
                        map.put("description", g.description);
                        map.put("qty", String.valueOf(g.qty));
                        map.put("unitPrice", formatVnd(g.unitVal));
                        map.put("amount", formatVnd(g.amount));
                        map.put("isChild", false);
                        items.add(map);

                        if (g.children != null) {
                            for (TempItem c : g.children) {
                                java.util.Map<String, Object> childMap = new java.util.HashMap<>();
                                childMap.put("date", "");
                                childMap.put("description", c.description);
                                childMap.put("qty", String.valueOf(c.qty));
                                
                                String formattedVal = c.unitVal.compareTo(java.math.BigDecimal.ZERO) == 0 ? "Miễn phí" : formatVnd(c.unitVal);
                                String formattedAmt = formatVnd(c.amount);
                                if (c.description.contains("bảo hiểm du lịch")) {
                                    formattedVal = formatVnd(c.unitVal);
                                    formattedAmt = "Đã bao gồm";
                                }

                                childMap.put("unitPrice", formattedVal);
                                childMap.put("amount", formattedAmt);
                                childMap.put("isChild", true);
                                items.add(childMap);
                            }
                        }
                    }

                    java.math.BigDecimal computedVat = computedSubtotal.multiply(new java.math.BigDecimal("0.10")).setScale(0, java.math.RoundingMode.HALF_UP);
                    java.math.BigDecimal computedTotal = computedSubtotal.add(computedVat).subtract(depositAmount);

                    ctx.setVariable("subtotal", formatVnd(computedSubtotal));
                    ctx.setVariable("vatAmount", formatVnd(computedVat));
                    ctx.setVariable("depositAmount", formatVnd(depositAmount));
                    ctx.setVariable("totalAmount", formatVnd(computedTotal));
                } else {
                    // Fallback to defaults
                    ctx.setVariable("subtotal", formatVnd(invoice.getSubtotalBeforeVat()));
                    ctx.setVariable("vatAmount", formatVnd(invoice.getVatAmount()));
                    ctx.setVariable("depositAmount", formatVnd(depositAmount));
                    ctx.setVariable("totalAmount", formatVnd(invoice.getTotalAmount().subtract(depositAmount)));
                }
            }

            ctx.setVariable("roomNumber", roomNumber.trim());
            ctx.setVariable("guestCount", guestCount);
            ctx.setVariable("checkInDate", checkInDateStr);
            ctx.setVariable("checkOutDate", checkOutDateStr);
            ctx.setVariable("items", items);
            ctx.setVariable("paymentMethod", paymentMethod);
            ctx.setVariable("vatAmount", formatVnd(invoice.getVatAmount()));
            ctx.setVariable("depositAmount", formatVnd(depositAmount));
            ctx.setVariable("totalAmount", formatVnd(invoice.getTotalAmount().subtract(depositAmount)));
            ctx.setVariable("issuedDate", invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DATE_FMT) : java.time.LocalDate.now().format(DATE_FMT));

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
        // Write a debug copy to a local file in the project directory for immediate verification
        try {
            java.nio.file.Path debugPath = java.nio.file.Paths.get("debug_email_invoice.html");
            java.nio.file.Files.writeString(debugPath, htmlContent, java.nio.charset.StandardCharsets.UTF_8);
            logger.info("[DEBUG] Đã ghi nội dung email ra file cục bộ để kiểm tra: {}", debugPath.toAbsolutePath());
        } catch (Exception e) {
            logger.error("[DEBUG] Không thể ghi file email debug: {}", e.getMessage());
        }

        boolean sentViaSmtp = false;

        if (mailSender != null) {
            try {
                jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
                org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                        mimeMessage, "utf-8");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                String finalFrom = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "hoanien.00@gmail.com";
                helper.setFrom(finalFrom, "HOANIEN Resort");

                mailSender.send(mimeMessage);
                logger.info("[SMTP] Email sent successfully to {}", toEmail);
                sentViaSmtp = true;
                return; // Gửi SMTP thành công thì thoát luôn
            } catch (Exception e) {
                logger.error("[SMTP] Lỗi gửi email tới {}: {}", toEmail, e.getMessage());
                logger.info("[SMTP] Sẽ thử chuyển sang dùng SendGrid fallback...");
            }
        }

        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            if (!sentViaSmtp) {
                logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Email không được gửi.");
                logger.info("[SENDGRID MOCK] To: {}, Subject: {}", toEmail, subject);
            }
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
        boolean sentViaSmtp = false;
        String finalFrom = (customFromEmail != null && !customFromEmail.trim().isEmpty()) ? customFromEmail : fromEmail;

        if (mailSender != null) {
            try {
                jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
                org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                        mimeMessage, "utf-8");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                helper.setFrom(finalFrom, "HOANIEN Workflow Engine");

                mailSender.send(mimeMessage);
                logger.info("[SMTP] Custom Workflow Email sent successfully from {} to {}", finalFrom, toEmail);
                sentViaSmtp = true;
                return;
            } catch (Exception e) {
                logger.error("[SMTP] Lỗi gửi Custom Workflow email tới {}: {}", toEmail, e.getMessage());
            }
        }

        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            if (!sentViaSmtp) {
                logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Custom Workflow Email không được gửi.");
                logger.info("[SENDGRID MOCK] From: {}, To: {}, Subject: {}", customFromEmail, toEmail, subject);
                logger.info("[SENDGRID MOCK CONTENT]: \n{}", htmlContent);
            }
            return;
        }

        try {
            // finalFrom has already been calculated above
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
            com.kawai.models.Customer customer, String cancelledBy, String reason) {
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
            ctx.setVariable("tableNumber", reservation.getTable() != null ? reservation.getTable().getTableNumber() : "N/A");
            ctx.setVariable("guestCount", reservation.getPartySize() != null ? reservation.getPartySize() : 0);
            ctx.setVariable("cancelledBy", cancelledBy != null && !cancelledBy.isBlank() ? cancelledBy : "Khách hàng");
            ctx.setVariable("reason", reason != null && !reason.isBlank() ? reason : "Không có");

            String html = templateEngine.process("email/cancel-table-booking", ctx);
            sendEmail(customer.getEmail(), "Xác nhận Hủy Đặt Bàn #" + reservation.getId(), html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email hủy đặt bàn #{}: {}", reservation.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendCancelFoodOrderEmail(com.kawai.models.FoodOrder order, com.kawai.models.Customer customer, String cancelledBy, String reason, java.math.BigDecimal refundAmount, String bankName, String accountName, String accountLast3) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank())
            return;
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("orderId", order.getId());
            ctx.setVariable("cancelTime", java.time.LocalDateTime.now().format(fmt));
            ctx.setVariable("orderTime", order.getOrderTime() != null ? order.getOrderTime().format(fmt) : "");
            ctx.setVariable("totalAmount", order.getTotalAmount());
            ctx.setVariable("cancelledBy", cancelledBy != null && !cancelledBy.isBlank() ? cancelledBy : "Khách hàng");
            ctx.setVariable("reason", reason != null && !reason.isBlank() ? reason : "Không có");
            
            ctx.setVariable("refundAmount", refundAmount);
            if (refundAmount != null && refundAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                ctx.setVariable("bankName", bankName != null ? bankName : "N/A");
                ctx.setVariable("accountName", accountName != null ? accountName : "N/A");
                ctx.setVariable("accountLast3", accountLast3 != null ? accountLast3 : "N/A");
            }

            String html = templateEngine.process("email/cancel-food-order", ctx);
            sendEmail(customer.getEmail(), "[HoaNien Resort] Xác nhận hủy đơn hàng Dịch vụ Ẩm thực #ORD-" + order.getId(), html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email hủy đơn hàng #{}: {}", order.getId(), e.getMessage());
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
            String subject = "[HOANIEN] Xác nhận hoàn tiền thành công";
            boolean sentViaSmtp = false;

            if (mailSender != null) {
                try {
                    jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
                    org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                            mimeMessage, true, "utf-8");
                    helper.setTo(customer.getEmail());
                    helper.setSubject(subject);
                    helper.setText(html, true);
                    String finalFrom = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "hoanien.00@gmail.com";
                    helper.setFrom(finalFrom, "HOANIEN Resort");

                    if (absoluteAttachmentPath != null && !absoluteAttachmentPath.isBlank()) {
                        java.io.File file = new java.io.File(absoluteAttachmentPath);
                        if (file.exists()) {
                            helper.addAttachment(file.getName(), file);
                        }
                    }

                    mailSender.send(mimeMessage);
                    logger.info("[SMTP] Sent refund email successfully to {}", customer.getEmail());
                    sentViaSmtp = true;
                } catch (Exception e) {
                    logger.error("[SMTP] Lỗi gửi email hoàn tiền tới {}: {}", customer.getEmail(), e.getMessage());
                    logger.info("[SMTP] Sẽ thử chuyển sang dùng SendGrid fallback...");
                }
            }

            if (!sentViaSmtp) {
                if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
                    logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Email hoàn tiền không được gửi.");
                    return;
                }

                Email from = new Email(fromEmail, "HOANIEN Resort");
                Email to = new Email(customer.getEmail());
                Content content = new Content("text/html", html);
                Mail mail = new Mail(from, subject, to, content);

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
                logger.info("[SENDGRID] Sent refund email to {}: status {}", customer.getEmail(),
                        response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Lỗi gửi email hoàn tiền cho RefundRequest #{}: {}", refundRequest.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendTourFeedbackEmail(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email cảm ơn: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            String rawCustName = customer.getFullName() != null ? customer.getFullName() : "Quý khách";
            String rawTourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName()
                    : "Hành trình trải nghiệm";
            ctx.setVariable("customerName", java.text.Normalizer.normalize(rawCustName, java.text.Normalizer.Form.NFC));
            ctx.setVariable("tourName", java.text.Normalizer.normalize(rawTourName, java.text.Normalizer.Form.NFC));
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("feedbackUrl", baseUrl + "/feedback?bookingId=" + booking.getId());
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            String html = templateEngine.process("email/tour-completed-feedback", ctx);
            sendEmail(customer.getEmail(), "Cảm ơn bạn đã tham gia hành trình cùng HoaNien", html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email cảm ơn và đánh giá cho booking #{}: {}", booking.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendFeedbackReplyEmail(com.kawai.models.Review review, com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email phản hồi: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            String rawCustName = customer.getFullName() != null ? customer.getFullName() : "Quý khách";
            String rawTourName = review.getTourBooking() != null && review.getTourBooking().getSchedule() != null
                    && review.getTourBooking().getSchedule().getTour() != null
                            ? review.getTourBooking().getSchedule().getTour().getTourName()
                            : "Hành trình trải nghiệm";

            ctx.setVariable("customerName", java.text.Normalizer.normalize(rawCustName, java.text.Normalizer.Form.NFC));
            ctx.setVariable("tourName", java.text.Normalizer.normalize(rawTourName, java.text.Normalizer.Form.NFC));

            String replierName = "Quản trị viên";
            if (review.getRepliedBy() != null) {
                replierName = review.getRepliedBy().getFullName();
            } else if (review.getTourBooking() != null && review.getTourBooking().getSchedule() != null) {
                Long scheduleId = review.getTourBooking().getSchedule().getId();
                java.util.List<com.kawai.models.TourStaffAssignment> assigns = tourStaffAssignmentRepository
                        .findByScheduleId(scheduleId);
                if (assigns != null && !assigns.isEmpty()) {
                    com.kawai.models.TourStaffAssignment lead = assigns.stream()
                            .filter(a -> Boolean.TRUE.equals(a.getIsLeadGuide()))
                            .findFirst()
                            .orElse(assigns.get(0));
                    if (lead.getEmployee() != null) {
                        replierName = lead.getEmployee().getFullName();
                    }
                }
            }

            ctx.setVariable("replierName", replierName);
            ctx.setVariable("reviewText", review.getReviewText());
            ctx.setVariable("replyText", review.getReplyText());
            ctx.setVariable("rating", review.getRatingTour() != null ? review.getRatingTour() : 5);
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            String html = templateEngine.process("email/tour-feedback-reply", ctx);
            sendEmail(customer.getEmail(), "Phản hồi về nhận xét hành trình từ HoaNien Retreat", html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email phản hồi nhận xét cho review #{}: {}", review.getId(), e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async
    public void sendTourDepartureEmail(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer,
            java.util.List<com.kawai.models.TourItineraryDetail> activities) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email khởi hành: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            String rawCustName = customer.getFullName() != null ? customer.getFullName() : "Quý khách";
            ctx.setVariable("customerName", java.text.Normalizer.normalize(rawCustName, java.text.Normalizer.Form.NFC));

            String tourName = "Hành trình trải nghiệm";
            String departureDate = "";
            String departureTime = "";
            String guideName = "NguynNgoc";

            if (booking.getSchedule() != null) {
                com.kawai.models.TourSchedule sched = booking.getSchedule();
                if (sched.getTour() != null)
                    tourName = sched.getTour().getTourName();
                if (sched.getDepartureDate() != null)
                    departureDate = sched.getDepartureDate().format(DATE_FMT);
                if (sched.getDepartureTime() != null)
                    departureTime = sched.getDepartureTime().toString().substring(0, 5);
                guideName = getGuideForSchedule(sched);
            }

            String normalizedTourName = java.text.Normalizer.normalize(tourName, java.text.Normalizer.Form.NFC);
            ctx.setVariable("tourName", normalizedTourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("departureTime", departureTime);
            ctx.setVariable("guideName", guideName);
            ctx.setVariable("activities", activities != null ? activities : java.util.Collections.emptyList());
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            String html = templateEngine.process("email/tour-departure-notification", ctx);
            sendEmail(customer.getEmail(),
                    "Hành trình " + normalizedTourName + " đã bắt đầu – Lịch trình chi tiết", html);
        } catch (Exception e) {
            logger.error("Lỗi gửi email khởi hành cho booking #{}: {}", booking.getId(), e.getMessage());
        }
    }

    private String getGuideForSchedule(com.kawai.models.TourSchedule sched) {
        if (sched == null)
            return "NguynNgoc";

        if (sched.getId() != null && sched.getId() < 100L) {
            return getPreferredGuide(sched);
        }

        java.time.LocalDate depDate = sched.getDepartureDate();
        java.time.LocalTime depTime = sched.getDepartureTime();

        if (depDate == null || depTime == null) {
            return "NguynNgoc";
        }

        if (tourScheduleRepository == null) {
            return "NguynNgoc";
        }

        java.util.List<com.kawai.models.TourSchedule> allSchedules = tourScheduleRepository.findAll();
        java.util.List<com.kawai.models.TourSchedule> conflictSchedules = new java.util.ArrayList<>();
        for (com.kawai.models.TourSchedule s : allSchedules) {
            if (depDate.equals(s.getDepartureDate()) && depTime.equals(s.getDepartureTime())) {
                conflictSchedules.add(s);
            }
        }
        conflictSchedules.sort((s1, s2) -> {
            Long id1 = s1.getId() != null ? s1.getId() : 0L;
            Long id2 = s2.getId() != null ? s2.getId() : 0L;
            return id1.compareTo(id2);
        });

        java.util.Set<String> taken = new java.util.HashSet<>();
        for (com.kawai.models.TourSchedule s : conflictSchedules) {
            if (s.getId() != null && s.getId() < 100L) {
                taken.add(getPreferredGuide(s));
            }
        }

        String assigned = null;
        for (com.kawai.models.TourSchedule s : conflictSchedules) {
            if (s.getId() != null && s.getId() < 100L) {
                if (s.getId().equals(sched.getId())) {
                    return getPreferredGuide(sched);
                }
                continue;
            }

            String g;
            if (!taken.contains("NguynNgoc")) {
                g = "NguynNgoc";
            } else if (!taken.contains("Ngọc Lan")) {
                g = "Ngọc Lan";
            } else {
                g = "Hoàng Nam";
            }
            taken.add(g);

            if (s.getId() != null && s.getId().equals(sched.getId())) {
                assigned = g;
                break;
            }
        }

        return assigned != null ? assigned : "NguynNgoc";
    }

    private String getPreferredGuide(com.kawai.models.TourSchedule sched) {
        if (sched == null)
            return "Ngọc Lan";
        if (sched.getId() != null && sched.getId() < 100L) {
            if (sched.getId() % 2 == 0) {
                return "Ngọc Lan";
            } else {
                return "Hoàng Nam";
            }
        }
        return "NguynNgoc";
    }

    @Override
    public void sendTableCancellationDueToCheckoutEmail(com.kawai.models.TableReservation reservation,
            com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isEmpty()) {
            return;
        }

        try {
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("customerName", customer.getFullName() != null ? customer.getFullName() : customer.getEmail());
            model.put("tableNumber", reservation.getTable().getTableNumber());
            model.put("reserveDate", reservation.getReserveDate().toString());
            model.put("reserveTime", reservation.getReserveTime().toString());

            String subject = "[HOANIEN Resort] Thông báo tự động hủy lịch đặt bàn";

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; color: #333;\">"
                    + "<div style=\"background-color: #2c3e50; padding: 20px; text-align: center;\">"
                    + "  <h2 style=\"color: #f1c40f; margin: 0;\">HOANIEN Resort</h2>"
                    + "</div>"
                    + "<div style=\"padding: 20px; background-color: #f9f9f9; border: 1px solid #ddd;\">"
                    + "  <p>Kính chào <strong>" + model.get("customerName") + "</strong>,</p>"
                    + "  <p>Do quý khách đã hoàn tất thủ tục trả phòng (Check-out), hệ thống đã tự động hủy lịch đặt bàn tại nhà hàng của chúng tôi với chi tiết như sau:</p>"
                    + "  <ul style=\"list-style-type: none; padding: 0;\">"
                    + "    <li style=\"margin-bottom: 10px;\"><strong>Bàn:</strong> " + model.get("tableNumber")
                    + "</li>"
                    + "    <li style=\"margin-bottom: 10px;\"><strong>Ngày đặt:</strong> " + model.get("reserveDate")
                    + "</li>"
                    + "    <li style=\"margin-bottom: 10px;\"><strong>Giờ đặt:</strong> " + model.get("reserveTime")
                    + "</li>"
                    + "  </ul>"
                    + "  <p style=\"color: #e74c3c; font-style: italic;\">Nếu quý khách vẫn muốn dùng bữa, xin vui lòng đặt lại bàn trực tiếp tại quầy lễ tân với tư cách khách vãng lai.</p>"
                    + "  <p>Cảm ơn quý khách đã tin tưởng và sử dụng dịch vụ của HOANIEN Resort.</p>"
                    + "  <hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\" />"
                    + "  <p style=\"font-size: 12px; color: #777;\">Đây là email tự động, vui lòng không phản hồi.</p>"
                    + "</div>"
                    + "</div>";

            sendEmail(customer.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendDependentUpgradeEmail(com.kawai.models.Customer masterCustomer,
            com.kawai.models.Customer newCustomer, String username, String password) {
        if (masterCustomer == null || masterCustomer.getEmail() == null || masterCustomer.getEmail().trim().isEmpty()) {
            return;
        }

        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
            ctx.setVariable("customerName", masterCustomer.getFullName());
            ctx.setVariable("username", username);
            ctx.setVariable("password", password);
            ctx.setVariable("resortName", "HOANIEN Resort");

            String htmlContent = templateEngine.process("email/account-upgrade", ctx);
            sendEmail(masterCustomer.getEmail(), "Thông báo nâng cấp tài khoản thành công", htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendRoomCancellationEmail(com.kawai.models.RoomBooking booking, com.kawai.models.Customer customer,
            boolean isRefundable) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            return;
        }

        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("resortName", "HOANIEN Resort");

            String templateName = isRefundable ? "email/room-cancelled-refund" : "email/room-cancelled-no-refund";
            String htmlContent = templateEngine.process(templateName, ctx);
            sendEmail(customer.getEmail(), "Xác nhận hủy đặt phòng - " + booking.getId(), htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendRoomNoShowEmail(com.kawai.models.RoomBooking booking, com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            return;
        }

        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
            ctx.setVariable("customerName", customer.getFullName());

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String checkInDateStr = booking.getCheckInDate() != null ? booking.getCheckInDate().format(formatter)
                    : "N/A";
            String cancelDateStr = java.time.LocalDate.now().format(formatter);

            ctx.setVariable("checkInDate", checkInDateStr);
            ctx.setVariable("cancelDate", cancelDateStr);

            String htmlContent = templateEngine.process("email/no-show-room-booking", ctx);
            sendEmail(customer.getEmail(), "[HOANIEN] Thông báo hủy đặt phòng - Khách không đến", htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendWalkInCheckInEmail(com.kawai.models.RoomBooking booking, com.kawai.models.RoomBookingDetail detail,
            com.kawai.models.Customer customer, boolean isNewAccount, String username, String password) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            return;
        }

        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("roomNumber", detail.getRoom() != null ? detail.getRoom().getRoomNumber() : "N/A");
            ctx.setVariable("roomCategory",
                    detail.getCategory() != null ? detail.getCategory().getCategoryName() : "N/A");
            ctx.setVariable("checkInDate",
                    booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "N/A");
            ctx.setVariable("checkOutDate",
                    booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "N/A");
            ctx.setVariable("resortName", "HOANIEN Resort");

            if (isNewAccount) {
                ctx.setVariable("username", username);
                ctx.setVariable("password", password);
            }

            String templateName = isNewAccount ? "email/walkin-checkin-new" : "email/walkin-checkin-existing";
            String htmlContent = templateEngine.process(templateName, ctx);
            sendEmail(customer.getEmail(), "Xác nhận nhận phòng (Check-in) - " + booking.getId(), htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendProfileUpdateEmail(com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email cập nhật hồ sơ: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            String rawCustName = customer.getFullName() != null ? customer.getFullName() : "Quý khách";
            ctx.setVariable("customerName", java.text.Normalizer.normalize(rawCustName, java.text.Normalizer.Form.NFC));
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            String htmlContent = templateEngine.process("email/profile-update", ctx);
            sendEmail(customer.getEmail(), "Cập nhật hồ sơ thành công | Hoa Niên Retreat & Resort", htmlContent);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email cập nhật hồ sơ cho KH {}: {}", customer.getId(), e.getMessage());
        }
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendWeeklyScheduleEmail(String toEmail, String employeeName,
            java.util.List<com.kawai.models.StaffSchedule> schedules) {
        try {
            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
            ctx.setVariable("employeeName", employeeName);
            ctx.setVariable("schedules", schedules);
            String htmlContent = templateEngine.process("email/weekly-schedule", ctx);
            sendEmail(toEmail, "Lịch Làm Việc Hàng Tuần - HOANIEN Resort", htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TempItem {
        String key = "";
        String date = "";
        String description = "";
        int qty = 1;
        java.math.BigDecimal unitVal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
        boolean isChild = false;
        boolean isParentTour = false;
        boolean isParentFnb = false;
        boolean isParentMinibar = false;
        java.util.List<TempItem> children = new java.util.ArrayList<>();
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendRoomBookingConfirmation(com.kawai.models.RoomBooking booking, com.kawai.models.Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email xác nhận đặt phòng: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }

        try {
            long numberOfNights = 0;
            if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                numberOfNights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(),
                        booking.getCheckOutDate());
                if (numberOfNights < 1)
                    numberOfNights = 1;
            }

            int numberOfGuests = 0;
            java.util.List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepository
                    .findByRoomBookingId(booking.getId());
            if (details != null && !details.isEmpty()) {
                for (com.kawai.models.RoomBookingDetail detail : details) {
                    if (detail.getNumberOfAdults() != null)
                        numberOfGuests += detail.getNumberOfAdults();
                    if (detail.getNumberOfChildren() != null)
                        numberOfGuests += detail.getNumberOfChildren();
                }
            }
            if (numberOfGuests == 0)
                numberOfGuests = 1; // Default if not found

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("checkInDate",
                    booking.getCheckInDate() != null ? booking.getCheckInDate().format(DATE_FMT) : "");
            ctx.setVariable("checkOutDate",
                    booking.getCheckOutDate() != null ? booking.getCheckOutDate().format(DATE_FMT) : "");
            ctx.setVariable("numberOfNights", numberOfNights);
            ctx.setVariable("numberOfGuests", numberOfGuests);
            ctx.setVariable("depositAmount", formatVnd(booking.getDepositAmount()));
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            String html = templateEngine.process("email/room-booking-confirmation", ctx);
            sendEmail(customer.getEmail(), "Xác nhận đặt phòng thành công | Hòa Niên Retreat & Resort", html);
            logger.info("Gửi email xác nhận đặt phòng thành công → {} (booking #{})", customer.getEmail(),
                    booking.getId());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email xác nhận đặt phòng cho booking #{}: {}", booking.getId(), e.getMessage(),
                    e);
        }
    }
}
