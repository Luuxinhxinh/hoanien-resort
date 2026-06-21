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

@RestController
@RequestMapping("/api/folios")
public class FolioRestController {

    private final NightAuditService nightAuditService;
    private final FolioItemRepository folioItemRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomRepository roomRepository;
    private final ConsolidatedInvoiceRepository consolidatedInvoiceRepository;
    private final PaymentService paymentService;
    private final InvoicePdfService invoicePdfService;
    private final EmailService emailService;

    @Autowired
    public FolioRestController(NightAuditService nightAuditService,
            FolioItemRepository folioItemRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomRepository roomRepository,
            ConsolidatedInvoiceRepository consolidatedInvoiceRepository,
            PaymentService paymentService,
            InvoicePdfService invoicePdfService,
            EmailService emailService) {
        this.nightAuditService = nightAuditService;
        this.folioItemRepository = folioItemRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomRepository = roomRepository;
        this.consolidatedInvoiceRepository = consolidatedInvoiceRepository;
        this.paymentService = paymentService;
        this.invoicePdfService = invoicePdfService;
        this.emailService = emailService;
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
        if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
            guestName = detail.getRoomBooking().getCustomer().getFullName();
        }
        
        String roomNumber = "N/A";
        if (detail.getRoom() != null && detail.getRoom().getRoomNumber() != null) {
            roomNumber = detail.getRoom().getRoomNumber();
        }

        List<FolioItem> items = null;
        try {
            items = nightAuditService.getFolioItems(roomBookingDetailId);
        } catch (Exception e) {}

        BigDecimal currentBalance = BigDecimal.ZERO;
        try {
            currentBalance = nightAuditService.calculateFolioBalance(roomBookingDetailId);
        } catch (Exception e) {}

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
        
        if (!hasRoomCharge && detail.getRoomCharge() != null && detail.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> roomMap = new java.util.HashMap<>();
            roomMap.put("id", -1L);
            roomMap.put("sourceDepartment", "Room");
            roomMap.put("amount", detail.getRoomCharge());
            String catName = detail.getCategory() != null ? detail.getCategory().getCategoryName() : "Room";
            roomMap.put("description", "Room Charge (Expected) - " + catName);
            roomMap.put("isSettledSeparately", false);
            roomMap.put("createdAt", java.time.LocalDateTime.now().toString());
            itemDTOs.add(0, roomMap); // Add to top
            
            // Adjust balance to include this unposted room charge
            currentBalance = currentBalance.add(detail.getRoomCharge());
        }

        BigDecimal prePaidDeposit = BigDecimal.ZERO;
        if (detail.getRoomBooking() != null) {
            try {
                List<PaymentTransaction> payments = paymentService.getPaymentsByBookingId(detail.getRoomBooking().getId());
                if (payments != null) {
                    for (PaymentTransaction pt : payments) {
                        if ("DEPOSIT".equalsIgnoreCase(pt.getTransactionType()) && pt.getStatus() == PaymentStatus.SUCCESS) {
                            if (pt.getAmount() != null) {
                                prePaidDeposit = prePaidDeposit.add(pt.getAmount());
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        }
        
        // Adjust balance by subtracting deposit
        currentBalance = currentBalance.subtract(prePaidDeposit);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("roomBookingDetailId", roomBookingDetailId);
        response.put("guestName", guestName);
        response.put("roomNumber", roomNumber);
        response.put("subCreditLimit", detail.getSubCreditLimit());
        response.put("items", itemDTOs);
        response.put("currentBalance", currentBalance);
        response.put("prePaidDeposit", prePaidDeposit);

        return ResponseEntity.ok(response);
    }

    /**
     * UC21.5 - Tách hóa đơn dịch vụ riêng lẻ
     */
    @PutMapping("/items/{folioItemId}/split")
    public ResponseEntity<?> splitFolioItem(@PathVariable Long folioItemId, @RequestBody Map<String, Boolean> payload) {
        Optional<FolioItem> optItem = folioItemRepository.findById(folioItemId);
        if (optItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FolioItem item = optItem.get();
        Boolean isSettledSeparately = payload.getOrDefault("isSettledSeparately", true);
        item.setIsSettledSeparately(isSettledSeparately);
        folioItemRepository.save(item);

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
            @RequestBody(required = false) Map<String, Object> payload) {
        Optional<RoomBookingDetail> optDetail = roomBookingDetailRepository.findById(roomBookingDetailId);
        if (optDetail.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phòng"));
        }
        RoomBookingDetail detail = optDetail.get();

        BigDecimal finalBalance = nightAuditService.calculateFolioBalance(roomBookingDetailId);
        BigDecimal paymentAmount = BigDecimal.ZERO;
        String paymentMethod = "CASH";

        if (payload != null && payload.containsKey("paymentAmount")) {
            paymentAmount = new BigDecimal(payload.get("paymentAmount").toString());
        }
        if (payload != null && payload.containsKey("paymentMethod")) {
            paymentMethod = payload.get("paymentMethod").toString();
        }

        // Trường hợp Balance != 0
        if (finalBalance.compareTo(BigDecimal.ZERO) > 0) {
            if (paymentAmount.compareTo(finalBalance) < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Khách hàng còn dư nợ " + finalBalance + ". Số tiền thanh toán chưa đủ."));
            }
        }

        // 1. Thay đổi trạng thái chi tiết phòng
        detail.setDetailStatus("Checked_Out");
        roomBookingDetailRepository.save(detail);

        // 2. Thay đổi trạng thái phòng vật lý qua Dirty
        Room room = detail.getRoom();
        if (room != null) {
            room.setRoomStatus("Vacant_Dirty");
            room.setCurrentBookingDetailId(null);
            roomRepository.save(room);
        }

        // 3. Tạo hóa đơn tổng (Consolidated Invoice)
        ConsolidatedInvoice invoice = new ConsolidatedInvoice();
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setBooking(detail.getRoomBooking());

        BigDecimal subtotal = finalBalance.divide(new BigDecimal("1.10"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal vat = finalBalance.subtract(subtotal);
        invoice.setSubtotalBeforeVat(subtotal);
        invoice.setVatAmount(vat);
        invoice.setTotalAmount(finalBalance);
        String initialInvoiceStatus = "CASH".equalsIgnoreCase(paymentMethod) ? "Paid" : "Unpaid";
        invoice.setInvoiceStatus(initialInvoiceStatus);
        invoice.setIssuedAt(LocalDateTime.now());
        consolidatedInvoiceRepository.save(invoice);

        // 4. Ghi nhận Payment Transaction (nếu có thanh toán)
        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            PaymentStatus initialStatus = "CASH".equalsIgnoreCase(paymentMethod) ? PaymentStatus.SUCCESS
                    : PaymentStatus.PENDING;
            paymentService.recordPayment(
                    invoice,
                    detail.getRoomBooking(),
                    paymentAmount,
                    "FINAL_PAYMENT",
                    paymentMethod,
                    initialStatus,
                    "TXN-" + System.currentTimeMillis());
        }

        // 5. Sinh file PDF hóa đơn và gửi email (Sử dụng Service)
        String pdfPath = invoicePdfService.generateInvoicePdf(invoice);
        String customerEmail = detail.getRoomBooking().getCustomer().getEmail();
        emailService.sendInvoiceEmail(customerEmail, invoice, pdfPath);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tất toán thành công. Đã tạo hóa đơn và đổi trạng thái phòng thành Vacant_Dirty.",
                "invoiceNumber", invoice.getInvoiceNumber()));
    }

    /**
     * Lấy danh sách các folio đang active (Checked_In)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveFolios() {
        try {
            List<RoomBookingDetail> activeDetails = roomBookingDetailRepository.findByDetailStatus("Checked_In");
            
            List<Map<String, Object>> result = activeDetails.stream().map(detail -> {
                BigDecimal balance = BigDecimal.ZERO;
                try {
                    balance = nightAuditService.calculateFolioBalance(detail.getId());
                } catch (Exception e) {}
                
                List<FolioItem> items = null;
                try {
                    items = nightAuditService.getFolioItems(detail.getId());
                } catch (Exception e) {}
                
                BigDecimal totalCharges = BigDecimal.ZERO;
                BigDecimal totalPayments = BigDecimal.ZERO;
                
                if (items != null) {
                    for (FolioItem item : items) {
                        if (item.getAmount() != null) {
                            if (item.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                                totalCharges = totalCharges.add(item.getAmount());
                            } else {
                                totalPayments = totalPayments.add(item.getAmount().abs());
                            }
                        }
                    }
                }

                Map<String, Object> map = new java.util.HashMap<>();
                map.put("folioNo", "FOL-" + String.format("%04d", detail.getId()));
                
                String roomNumber = "N/A";
                if (detail.getRoom() != null && detail.getRoom().getRoomNumber() != null) {
                    roomNumber = detail.getRoom().getRoomNumber();
                }
                map.put("roomNumber", roomNumber);
                
                String guestName = "Unknown";
                if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
                    guestName = detail.getRoomBooking().getCustomer().getFullName();
                }
                map.put("guestName", guestName);
                
                map.put("totalCharges", totalCharges);
                map.put("totalPayments", totalPayments);
                map.put("balance", balance);
                map.put("status", detail.getDetailStatus());
                map.put("roomBookingDetailId", detail.getId());
                return map;
            }).toList();
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResp = new java.util.HashMap<>();
            errorResp.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.status(500).body(errorResp);
        }
    }
}
