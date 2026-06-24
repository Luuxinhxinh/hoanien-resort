package com.kawai.controllers.api;

import com.kawai.models.FolioItem;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.services.interfaces.NightAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kawai.models.ConsolidatedInvoice;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.ConsolidatedInvoiceRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.EmailService;
import com.kawai.services.interfaces.InvoicePdfService;
import com.kawai.services.interfaces.PaymentService;
import com.kawai.models.PaymentTransaction;
import com.kawai.models.PaymentStatus;
import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import com.kawai.services.interfaces.VnPayService;

@RestController
@RequestMapping("/api/folios")
public class FolioRestController {

    @Autowired
    private com.kawai.services.interfaces.WorkflowEngineService workflowEngineService;

    private final NightAuditService nightAuditService;
    private final FolioItemRepository folioItemRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomRepository roomRepository;
    private final ConsolidatedInvoiceRepository consolidatedInvoiceRepository;
    private final PaymentService paymentService;
    private final InvoicePdfService invoicePdfService;
    private final EmailService emailService;
    private final VnPayService vnPayService;
    private final com.kawai.repositories.RoomBookingRepository roomBookingRepository;
    private final com.kawai.repositories.PromotionRepository promotionRepository;
    private final com.kawai.repositories.CustomerRepository customerRepository;

    @Autowired
    public FolioRestController(NightAuditService nightAuditService,
            FolioItemRepository folioItemRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomRepository roomRepository,
            ConsolidatedInvoiceRepository consolidatedInvoiceRepository,
            PaymentService paymentService,
            InvoicePdfService invoicePdfService,
            EmailService emailService,
            VnPayService vnPayService,
            com.kawai.repositories.RoomBookingRepository roomBookingRepository,
            com.kawai.repositories.PromotionRepository promotionRepository,
            com.kawai.repositories.CustomerRepository customerRepository) {
        this.nightAuditService = nightAuditService;
        this.folioItemRepository = folioItemRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.roomRepository = roomRepository;
        this.consolidatedInvoiceRepository = consolidatedInvoiceRepository;
        this.paymentService = paymentService;
        this.invoicePdfService = invoicePdfService;
        this.emailService = emailService;
        this.vnPayService = vnPayService;
        this.promotionRepository = promotionRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Lấy danh sách các phòng cùng khách hàng đang Checked_In (để hiển thị tab)
     */
    @GetMapping("/booking-group/{roomBookingDetailId}")
    public ResponseEntity<?> getBookingGroup(@PathVariable Long roomBookingDetailId) {
        Optional<RoomBookingDetail> optDetail = roomBookingDetailRepository.findById(roomBookingDetailId);
        if (optDetail.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "RoomBookingDetail không tồn tại"));
        }
        RoomBookingDetail detail = optDetail.get();
        if (detail.getRoomBooking() == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Không có thông tin Booking"));
        }
        Long bookingId = detail.getRoomBooking().getId();

        List<RoomBookingDetail> allActive = roomBookingDetailRepository.findByDetailStatus("Checked_In");
        List<Map<String, Object>> rooms = new java.util.ArrayList<>();

        BigDecimal groupTotalCharges = BigDecimal.ZERO;
        BigDecimal groupBalance = BigDecimal.ZERO;

        for (RoomBookingDetail d : allActive) {
            if (d.getRoomBooking() != null && d.getRoomBooking().getId().equals(bookingId)) {
                Map<String, Object> rMap = new java.util.HashMap<>();
                rMap.put("id", d.getId());
                rMap.put("roomNumber", d.getRoom() != null ? d.getRoom().getRoomNumber() : "N/A");
                rooms.add(rMap);

                boolean hasRoomCharge = false;

                // Calculate balance for this room (excludes split items by default inside service)
                try {
                    BigDecimal roomBal = nightAuditService.calculateFolioBalance(d.getId());
                    groupBalance = groupBalance.add(roomBal);
                } catch (Exception e) {
                }

                // Calculate charges
                try {
                    List<FolioItem> items = nightAuditService.getFolioItems(d.getId());
                    if (items != null) {
                        for (FolioItem item : items) {
                            if (Boolean.TRUE.equals(item.getIsSettledSeparately())) {
                                continue; // Exclude split items!
                            }
                            if ("Room".equalsIgnoreCase(item.getSourceDepartment())) {
                                hasRoomCharge = true;
                            }
                            if (item.getAmount() != null && item.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                                groupTotalCharges = groupTotalCharges.add(item.getAmount());
                            }
                        }
                    }
                } catch (Exception e) {
                }

            }
        }

        BigDecimal totalPayments = BigDecimal.ZERO;
        try {
            List<PaymentTransaction> payments = paymentService.getPaymentsByBookingId(bookingId);
            if (payments != null) {
                for (PaymentTransaction pt : payments) {
                    if (pt.getStatus() == PaymentStatus.SUCCESS && pt.getAmount() != null) {
                        totalPayments = totalPayments.add(pt.getAmount());
                    }
                }
            }
        } catch (Exception e) {
        }

        groupBalance = groupBalance.multiply(new BigDecimal("1.10")).subtract(totalPayments);
        if (groupBalance.compareTo(BigDecimal.ZERO) < 0) {
            groupBalance = BigDecimal.ZERO;
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "rooms", rooms,
                "groupTotalCharges", groupTotalCharges,
                "groupTotalPayments", totalPayments,
                "groupBalance", groupBalance));
    }

    /**
     * UC21.1 - Lấy thông tin Ví Folio của phòng theo ID chi tiết phòng
     */
    @GetMapping("/room/{roomBookingDetailId}")
    public ResponseEntity<?> getFolioByRoom(@PathVariable Long roomBookingDetailId) {
        Optional<RoomBookingDetail> optDetail = roomBookingDetailRepository.findById(roomBookingDetailId);
        if (optDetail.isEmpty()) {
            Map<String, String> err = new java.util.HashMap<>();
            err.put("message", "RoomBookingDetail không tồn tại");
            return ResponseEntity.badRequest().body(err);
        }
        RoomBookingDetail detail = optDetail.get();

        String guestName = "Unknown";
        String bookerName = "Unknown";

        if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
            bookerName = detail.getRoomBooking().getCustomer().getFullName();
        }

        if (detail.getCustomer() != null) {
            guestName = detail.getCustomer().getFullName();
        } else {
            guestName = bookerName;
        }

        String roomNumber = "N/A";
        if (detail.getRoom() != null && detail.getRoom().getRoomNumber() != null) {
            roomNumber = detail.getRoom().getRoomNumber();
        }

        List<FolioItem> items = null;
        try {
            items = nightAuditService.getFolioItems(roomBookingDetailId);
        } catch (Exception e) {
        }

        BigDecimal currentBalance = BigDecimal.ZERO;
        try {
            currentBalance = nightAuditService.calculateFolioBalance(roomBookingDetailId);
        } catch (Exception e) {
        }

        List<Map<String, Object>> itemDTOs = new java.util.ArrayList<>();
        boolean hasRoomCharge = false;

        if (items != null) {
            for (FolioItem item : items) {
                if ("Room".equalsIgnoreCase(item.getSourceDepartment())) {
                    hasRoomCharge = true;
                }
                Map<String, Object> iMap = new java.util.HashMap<>();
                iMap.put("id", item.getId());
                iMap.put("sourceDepartment", item.getSourceDepartment());
                iMap.put("amount", item.getAmount());
                iMap.put("description", item.getDescription());
                iMap.put("isSettledSeparately", item.getIsSettledSeparately());
                iMap.put("createdAt", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
                itemDTOs.add(iMap);
            }
        }


        BigDecimal totalPayments = BigDecimal.ZERO;
        if (detail.getRoomBooking() != null) {
            try {
                List<PaymentTransaction> payments = paymentService
                        .getPaymentsByBookingId(detail.getRoomBooking().getId());
                if (payments != null) {
                    for (PaymentTransaction pt : payments) {
                        if (pt.getStatus() == PaymentStatus.SUCCESS) {
                            if (pt.getAmount() != null) {
                                totalPayments = totalPayments.add(pt.getAmount());
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        // Adjust balance by subtracting payments
        currentBalance = currentBalance.multiply(new BigDecimal("1.10")).subtract(totalPayments);
        if (currentBalance.compareTo(BigDecimal.ZERO) < 0) {
            currentBalance = BigDecimal.ZERO;
        }

        String checkInDate = "N/A";
        String checkOutDate = "N/A";
        if (detail.getRoomBooking() != null) {
            if (detail.getRoomBooking().getCheckInDate() != null) {
                checkInDate = detail.getRoomBooking().getCheckInDate().toString();
            }
            if (detail.getRoomBooking().getCheckOutDate() != null) {
                checkOutDate = detail.getRoomBooking().getCheckOutDate().toString();
            }
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("roomBookingDetailId", roomBookingDetailId);
        response.put("guestName", guestName);
        response.put("bookerName", bookerName);
        response.put("roomNumber", roomNumber);
        response.put("checkInDate", checkInDate);
        response.put("checkOutDate", checkOutDate);
        response.put("subCreditLimit", detail.getSubCreditLimit());
        response.put("items", itemDTOs);
        response.put("currentBalance", currentBalance);
        response.put("prePaidDeposit", totalPayments);
        response.put("roomCharge", detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);

        return ResponseEntity.ok(response);
    }

    /**
     * UC21.5 - Tách hóa đơn dịch vụ riêng lẻ
     */
    @PutMapping("/items/{folioItemId}/split")
    public ResponseEntity<?> splitFolioItem(@PathVariable Long folioItemId, @RequestBody Map<String, Boolean> payload) {
        FolioItem item;
        Boolean isSettledSeparately = payload.getOrDefault("isSettledSeparately", true);

        if (folioItemId < 0) {
            // Negative ID means it's an expected room charge. Create a real FolioItem on
            // the fly.
            Long detailId = Math.abs(folioItemId);
            Optional<RoomBookingDetail> optDetail = roomBookingDetailRepository.findById(detailId);
            if (optDetail.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            RoomBookingDetail detail = optDetail.get();
            item = new FolioItem();
            item.setRoomBookingDetail(detail);
            item.setSourceDepartment("Room");
            item.setAmount(detail.getRoomCharge());
            String catName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "Room";
            item.setDescription("Room Charge (Expected) - " + catName);
            item.setIsSettledSeparately(isSettledSeparately);
            item.setCreatedAt(java.time.LocalDateTime.now());
            folioItemRepository.save(item);
        } else {
            Optional<FolioItem> optItem = folioItemRepository.findById(folioItemId);
            if (optItem.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            item = optItem.get();
            item.setIsSettledSeparately(isSettledSeparately);
            folioItemRepository.save(item);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật trạng thái tách hóa đơn thành công",
                "item", item));
    }

    /**
     * UC21.3 & UC22.1 - Gom Folio và Tất toán (Có xử lý Payment & Invoice)
     */
    @PostMapping("/room/{roomBookingDetailId}/checkout")
    public ResponseEntity<?> checkoutFolio(@PathVariable Long roomBookingDetailId,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest httpRequest) {
        try {
            Optional<RoomBookingDetail> optDetail = roomBookingDetailRepository.findById(roomBookingDetailId);
            if (optDetail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phòng"));
            }
            RoomBookingDetail detail = optDetail.get();
            com.kawai.models.RoomBooking booking = detail.getRoomBooking();


            // Auto-post Room Charge if it hasn't been posted yet
            List<FolioItem> items = nightAuditService.getFolioItems(roomBookingDetailId);
            boolean hasRoomCharge = false;
            if (items != null) {
                for (FolioItem item : items) {
                    if ("Room".equalsIgnoreCase(item.getSourceDepartment())) {
                        hasRoomCharge = true;
                        break;
                    }
                }
            }
            if (!hasRoomCharge && detail.getRoomCharge() != null
                    && detail.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
                FolioItem item = new FolioItem();
                item.setRoomBookingDetail(detail);
                item.setBooking(booking);
                item.setPayerCustomer(booking != null ? booking.getCustomer() : null);
                item.setSourceDepartment("Room");
                item.setAmount(detail.getRoomCharge());
                String catName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "Room";
                item.setDescription("Room Charge (Expected) - " + catName);
                item.setIsSettledSeparately(false);
                item.setCreatedAt(java.time.LocalDateTime.now());
                folioItemRepository.save(item);
            }

            BigDecimal finalBalance = nightAuditService.calculateFolioBalance(roomBookingDetailId);
            finalBalance = finalBalance.multiply(new BigDecimal("1.10"));

            com.kawai.models.Promotion appliedPromo = null;
            if (payload != null && payload.containsKey("promoCode")) {
                String pCode = payload.get("promoCode").toString();
                Optional<com.kawai.models.Promotion> optP = promotionRepository.findByPromoCode(pCode);
                if (optP.isPresent()) {
                    com.kawai.models.Promotion p = optP.get();
                    if (Boolean.TRUE.equals(p.getIsActive())
                            && (p.getValidTo() == null || !p.getValidTo().isBefore(java.time.LocalDate.now()))) {
                        appliedPromo = p;
                        BigDecimal discountAmount;
                        BigDecimal discountRate = p.getDiscountValue();
                        if (discountRate.compareTo(new BigDecimal("100")) < 0) {
                            discountAmount = finalBalance.multiply(discountRate).divide(new BigDecimal("100"), 2,
                                    java.math.RoundingMode.HALF_UP);
                        } else {
                            discountAmount = discountRate;
                        }
                        if (discountAmount.compareTo(finalBalance) > 0) {
                            discountAmount = finalBalance;
                        }
                        finalBalance = finalBalance.subtract(discountAmount);
                    }
                }
            }

            if (booking != null && booking.getDepositAmount() != null) {
                BigDecimal deposit = booking.getDepositAmount();
                if (deposit.compareTo(BigDecimal.ZERO) > 0) {
                    if (finalBalance.compareTo(deposit) <= 0) {
                        BigDecimal used = finalBalance;
                        finalBalance = BigDecimal.ZERO;
                        booking.setDepositAmount(deposit.subtract(used));
                    } else {
                        finalBalance = finalBalance.subtract(deposit);
                        booking.setDepositAmount(BigDecimal.ZERO);
                    }
                }
            }

            BigDecimal paymentAmount = BigDecimal.ZERO;
            String paymentMethod = "CASH";

            if (payload != null && payload.containsKey("paymentAmount")) {
                paymentAmount = new BigDecimal(payload.get("paymentAmount").toString());
            }
            if (payload != null && payload.containsKey("paymentMethod")) {
                paymentMethod = payload.get("paymentMethod").toString();
            }

            BigDecimal bookingOutstanding = booking != null ? getBookingOutstandingBalance(booking) : finalBalance;
            BigDecimal minRequired = finalBalance.compareTo(bookingOutstanding) < 0 ? finalBalance : bookingOutstanding;

            // Trường hợp Balance != 0
            if (minRequired.compareTo(BigDecimal.ZERO) > 0) {
                if (paymentAmount.compareTo(minRequired) < 0) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Khách hàng còn dư nợ " + minRequired + ". Số tiền thanh toán chưa đủ."));
                }
            }

            boolean isVnPay = "VNPAY".equalsIgnoreCase(paymentMethod);

            // 1 & 2. Thay đổi trạng thái phòng (CHỈ làm ngay nếu KHÔNG PHẢI VNPAY)
            if (!isVnPay) {
                detail.setDetailStatus("Checked_Out");
                roomBookingDetailRepository.save(detail);

                // 2. Thay đổi trạng thái phòng vật lý qua Dirty (hoặc theo cấu hình workflow)
                Room room = detail.getRoom();
                if (room != null) {
                    boolean triggered = false;
                    try {
                        workflowEngineService.triggerEvent("ROOM_CHECKOUT", Map.of(
                                "room_id", room.getId(),
                                "booking_id", detail.getRoomBooking().getId(),
                                "booking_detail_id", detail.getId()));
                        triggered = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!triggered) {
                        room.setRoomStatus("Vacant_Dirty");
                        room.setCurrentBookingDetailId(null);
                        roomRepository.save(room);
                    }
                }
            }

            // 3. Tạo hóa đơn tổng (Consolidated Invoice) hoặc cập nhật nếu đã có
            ConsolidatedInvoice invoice = consolidatedInvoiceRepository.findByBooking_Id(booking.getId())
                    .orElse(new ConsolidatedInvoice());
            if (invoice.getInvoiceNumber() == null) {
                invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                invoice.setBooking(booking);
            }
            if (appliedPromo != null) {
                invoice.setPromo(appliedPromo);
            }

            BigDecimal currentTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal newTotal = currentTotal.add(finalBalance);

            BigDecimal subtotal = newTotal.divide(new BigDecimal("1.10"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal vat = newTotal.subtract(subtotal);
            invoice.setSubtotalBeforeVat(subtotal);
            invoice.setVatAmount(vat);
            invoice.setTotalAmount(newTotal);

            boolean allCheckedOut = false;
            if (booking != null) {
                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(booking.getId());
                allCheckedOut = details.stream()
                        .allMatch(d -> "Checked_Out".equals(d.getDetailStatus()) || d.getId().equals(detail.getId()));
                if (allCheckedOut && !isVnPay) {
                    booking.setBookingStatus("Completed");
                }
                if (!isVnPay) {
                    roomBookingRepository.save(booking);
                }
            }

            if (allCheckedOut) {
                String initialInvoiceStatus = isVnPay ? "Unpaid" : "Paid";
                invoice.setInvoiceStatus(initialInvoiceStatus);
            } else {
                invoice.setInvoiceStatus("Partial_Paid");
            }
            invoice.setIssuedAt(LocalDateTime.now());
            consolidatedInvoiceRepository.save(invoice);

            // 4. Ghi nhận Payment Transaction (nếu có thanh toán)
            PaymentTransaction txn = null;
            if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                PaymentStatus initialStatus = isVnPay ? PaymentStatus.PENDING : PaymentStatus.SUCCESS;
                String txnRef = isVnPay ? "FOLIO_" + detail.getId() + "_" + System.currentTimeMillis()
                        : "TXN-" + System.currentTimeMillis();
                txn = paymentService.recordPayment(
                        invoice,
                        detail.getRoomBooking(),
                        paymentAmount,
                        "FINAL_PAYMENT",
                        paymentMethod,
                        initialStatus,
                        txnRef);
            }

            // 5. Sinh file PDF hóa đơn và gửi email (Sử dụng Service) nếu đã thanh toán
            if ("Paid".equalsIgnoreCase(invoice.getInvoiceStatus())) {
                String pdfPath = invoicePdfService.generateInvoicePdf(invoice);
                String customerEmail = detail.getRoomBooking().getCustomer().getEmail();
                emailService.sendInvoiceEmail(customerEmail, invoice, pdfPath);

                // 5.1 Cộng điểm Loyalty (1 điểm = 10,000 VNĐ chi tiêu)
                if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                    com.kawai.models.Customer customer = detail.getRoomBooking().getCustomer();
                    if (customer != null) {
                        int pointsEarned = paymentAmount.divide(new BigDecimal("10000"), 0, java.math.RoundingMode.DOWN)
                                .intValue();
                        if (pointsEarned > 0) {
                            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
                            int newPoints = currentPoints + pointsEarned;
                            customer.setLoyaltyPoints(newPoints);

                            // Auto upgrade tier based on new points
                            String newTier = "Regular";
                            if (newPoints >= 20000)
                                newTier = "Diamond";
                            else if (newPoints >= 5000)
                                newTier = "Gold";
                            else if (newPoints >= 1000)
                                newTier = "Silver";

                            customer.setMembershipTier(newTier);
                            customerRepository.save(customer);
                            System.out.println("[LOYALTY] Khách " + customer.getFullName() + " vừa nhận " + pointsEarned
                                    + " điểm. Tổng: " + newPoints + " (" + newTier + ")");
                        }
                    }
                }
            }

            // 6. Trả về URL thanh toán VNPay nếu phương thức là VNPAY
            if ("VNPAY".equalsIgnoreCase(paymentMethod) && txn != null) {
                String paymentUrl = vnPayService.createPaymentUrlFromTransaction(txn, httpRequest.getRemoteAddr());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Chuyển hướng đến VNPay để thanh toán",
                        "paymentUrl", paymentUrl));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tất toán thành công. Đã tạo hóa đơn và đổi trạng thái phòng thành Vacant_Dirty.",
                    "invoiceNumber", invoice.getInvoiceNumber()));
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("error.log"),
                        t.toString() + "\n" +
                                java.util.Arrays.stream(t.getStackTrace())
                                        .map(StackTraceElement::toString)
                                        .collect(java.util.stream.Collectors.joining("\n")),
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            } catch (Exception e) {
            }
            throw t;
        }
    }

    private BigDecimal getBookingOutstandingBalance(com.kawai.models.RoomBooking booking) {
        BigDecimal totalCharges = BigDecimal.ZERO;
        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(booking.getId());
        for (RoomBookingDetail d : details) {
            // Room charges (posted)
            try {
                BigDecimal roomBal = nightAuditService.calculateFolioBalance(d.getId());
                totalCharges = totalCharges.add(roomBal);
            } catch (Exception e) {
            }
        }

        // Subtract all successful payments
        BigDecimal totalPayments = BigDecimal.ZERO;
        try {
            List<PaymentTransaction> payments = paymentService.getPaymentsByBookingId(booking.getId());
            if (payments != null) {
                for (PaymentTransaction pt : payments) {
                    if (pt.getStatus() == PaymentStatus.SUCCESS) {
                        if (pt.getAmount() != null) {
                            totalPayments = totalPayments.add(pt.getAmount());
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        BigDecimal outstanding = totalCharges.multiply(new BigDecimal("1.10")).subtract(totalPayments);
        return outstanding.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : outstanding;
    }

    /**
     * Lấy danh sách các folio đang active (Checked_In)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveFolios() {
        try {
            List<RoomBookingDetail> activeDetails = roomBookingDetailRepository.findByDetailStatus("Checked_In");

            // Group by Booking ID instead of Customer ID
            Map<Long, List<RoomBookingDetail>> groupedByBooking = activeDetails.stream()
                    .filter(d -> d.getRoomBooking() != null)
                    .collect(java.util.stream.Collectors.groupingBy(d -> d.getRoomBooking().getId()));

            List<Map<String, Object>> result = new java.util.ArrayList<>();

            for (Map.Entry<Long, List<RoomBookingDetail>> entry : groupedByBooking.entrySet()) {
                Long bookingId = entry.getKey();
                List<RoomBookingDetail> details = entry.getValue();

                BigDecimal groupTotalCharges = BigDecimal.ZERO;
                BigDecimal groupTotalPayments = BigDecimal.ZERO;
                BigDecimal groupBalance = BigDecimal.ZERO;
                List<String> roomNumbers = new java.util.ArrayList<>();

                String bookerName = "Unknown";
                if (!details.isEmpty() && details.get(0).getRoomBooking().getCustomer() != null) {
                    bookerName = details.get(0).getRoomBooking().getCustomer().getFullName();
                }

                for (RoomBookingDetail detail : details) {
                    if (detail.getRoom() != null && detail.getRoom().getRoomNumber() != null) {
                        roomNumbers.add(detail.getRoom().getRoomNumber());
                    }

                    BigDecimal balance = BigDecimal.ZERO;
                    try {
                        balance = nightAuditService.calculateFolioBalance(detail.getId());
                    } catch (Exception e) {
                    }
                    groupBalance = groupBalance.add(balance);

                    List<FolioItem> items = null;
                    try {
                        items = nightAuditService.getFolioItems(detail.getId());
                    } catch (Exception e) {
                    }

                    boolean hasRoomCharge = false;

                    if (items != null) {
                        for (FolioItem item : items) {
                            if (Boolean.TRUE.equals(item.getIsSettledSeparately())) {
                                continue; // Exclude split items!
                            }
                            if ("Room".equalsIgnoreCase(item.getSourceDepartment())) {
                                hasRoomCharge = true;
                            }
                            if (item.getAmount() != null) {
                                if (item.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                                    groupTotalCharges = groupTotalCharges.add(item.getAmount());
                                } else {
                                    groupTotalPayments = groupTotalPayments.add(item.getAmount().abs());
                                }
                            }
                        }
                    }

                }

                // Sum all successful payments for this booking
                BigDecimal totalPayments = BigDecimal.ZERO;
                try {
                    List<PaymentTransaction> payments = paymentService.getPaymentsByBookingId(bookingId);
                    if (payments != null) {
                        for (PaymentTransaction pt : payments) {
                            if (pt.getStatus() == PaymentStatus.SUCCESS && pt.getAmount() != null) {
                                totalPayments = totalPayments.add(pt.getAmount());
                            }
                        }
                    }
                } catch (Exception e) {
                }

                groupBalance = groupBalance.multiply(new BigDecimal("1.10")).subtract(totalPayments);
                if (groupBalance.compareTo(BigDecimal.ZERO) < 0) {
                    groupBalance = BigDecimal.ZERO;
                }
                groupTotalPayments = groupTotalPayments.add(totalPayments);
                groupTotalCharges = groupTotalCharges.multiply(new BigDecimal("1.10"));

                Map<String, Object> map = new java.util.HashMap<>();

                map.put("folioNo", "BKG-" + String.format("%04d", bookingId));
                map.put("roomNumber", String.join(", ", roomNumbers));
                map.put("bookerName", bookerName);
                map.put("totalCharges", groupTotalCharges);
                map.put("totalPayments", groupTotalPayments);
                map.put("balance", groupBalance);
                map.put("status", "Checked_In");

                if (!details.isEmpty()) {
                    map.put("roomBookingDetailId", details.get(0).getId());
                }

                result.add(map);
            }

            // Sort by folioNo descending
            result.sort((a, b) -> ((String) b.get("folioNo")).compareTo((String) a.get("folioNo")));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResp = new java.util.HashMap<>();
            errorResp.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.status(500).body(errorResp);
        }
    }

    @GetMapping("/promo/validate")
    public ResponseEntity<?> validatePromo(@RequestParam String code, @RequestParam BigDecimal amount) {
        try {
            Optional<com.kawai.models.Promotion> optPromo = promotionRepository.findByPromoCode(code);
            if (optPromo.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Mã giảm giá không tồn tại"));
            }
            com.kawai.models.Promotion promo = optPromo.get();
            if (!Boolean.TRUE.equals(promo.getIsActive())
                    || (promo.getValidTo() != null && promo.getValidTo().isBefore(java.time.LocalDate.now()))) {
                return ResponseEntity
                        .ok(Map.of("success", false, "message", "Mã giảm giá đã hết hạn hoặc không hoạt động"));
            }
            BigDecimal discountAmount;
            BigDecimal discountRate = promo.getDiscountValue();
            String discountType = discountRate.compareTo(new BigDecimal("100")) < 0 ? "PERCENTAGE" : "FIXED_AMOUNT";
            if ("FIXED_AMOUNT".equalsIgnoreCase(discountType)) {
                discountAmount = promo.getDiscountValue();
            } else {
                discountAmount = amount.multiply(discountRate).divide(new BigDecimal("100"), 2,
                        java.math.RoundingMode.HALF_UP);
            }
            if (discountAmount.compareTo(amount) > 0) {
                discountAmount = amount;
            }
            BigDecimal newAmount = amount.subtract(discountAmount);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "promoCode", promo.getPromoCode(),
                    "discountType", discountType,
                    "discountValue", discountRate,
                    "discountAmount", discountAmount,
                    "newAmount", newAmount));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Lỗi kiểm tra mã giảm giá: " + e.getMessage()));
        }
    }
}
