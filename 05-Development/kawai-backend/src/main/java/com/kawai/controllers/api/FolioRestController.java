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

    private final com.kawai.services.interfaces.WorkflowEngineService workflowEngineService;
    private final NightAuditService nightAuditService;
    private final FolioItemRepository folioItemRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomRepository roomRepository;
    private final ConsolidatedInvoiceRepository consolidatedInvoiceRepository;
    private final PaymentService paymentService;
    private final InvoicePdfService invoicePdfService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final VnPayService vnPayService;
    private final com.kawai.repositories.RoomBookingRepository roomBookingRepository;
    private final com.kawai.repositories.PromotionRepository promotionRepository;
    private final com.kawai.repositories.CustomerRepository customerRepository;
    private final com.kawai.repositories.RoomGuestRepository roomGuestRepository;
    private final com.kawai.repositories.MembershipTierRepository membershipTierRepository;
    private final com.kawai.repositories.HousekeepingTaskRepository housekeepingTaskRepo;
    private final com.kawai.repositories.EmployeeRepository employeeRepository;
    private final com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    public FolioRestController(NightAuditService nightAuditService,
            FolioItemRepository folioItemRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomRepository roomRepository,
            ConsolidatedInvoiceRepository consolidatedInvoiceRepository,
            PaymentService paymentService,
            InvoicePdfService invoicePdfService,
            org.springframework.context.ApplicationEventPublisher eventPublisher,
            VnPayService vnPayService,
            com.kawai.repositories.RoomBookingRepository roomBookingRepository,
            com.kawai.repositories.PromotionRepository promotionRepository,
            com.kawai.repositories.CustomerRepository customerRepository,
            com.kawai.repositories.RoomGuestRepository roomGuestRepository,
            com.kawai.repositories.MembershipTierRepository membershipTierRepository,
            com.kawai.services.interfaces.WorkflowEngineService workflowEngineService,
            com.kawai.repositories.HousekeepingTaskRepository housekeepingTaskRepo,
            com.kawai.repositories.EmployeeRepository employeeRepository,
            com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository) {
        this.nightAuditService = nightAuditService;
        this.folioItemRepository = folioItemRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.roomRepository = roomRepository;
        this.consolidatedInvoiceRepository = consolidatedInvoiceRepository;
        this.paymentService = paymentService;
        this.invoicePdfService = invoicePdfService;
        this.eventPublisher = eventPublisher;
        this.vnPayService = vnPayService;
        this.promotionRepository = promotionRepository;
        this.customerRepository = customerRepository;
        this.roomGuestRepository = roomGuestRepository;
        this.membershipTierRepository = membershipTierRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.workflowEngineService = workflowEngineService;
        this.housekeepingTaskRepo = housekeepingTaskRepo;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Lấy thông tin hạn mức tín dụng của tất cả phòng trong một Booking.
     * Dùng cho Modal "Nâng hạn mức" trên trang In-House.
     */
    @GetMapping("/booking/{bookingId}/credit-info")
    public ResponseEntity<?> getCreditInfoByBooking(@PathVariable Long bookingId) {
        try {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
            if (details.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Không tìm thấy phòng trong booking!"));
            }

            List<Map<String, Object>> rooms = new java.util.ArrayList<>();
            for (RoomBookingDetail d : details) {
                // Dùng native query lấy trực tiếp amount — tránh lỗi Hibernate mapping
                // khi DB có row cũ vi phạm payer_customer_id NOT NULL
                List<java.math.BigDecimal> amounts = folioItemRepository.findAmountsByDetailId(d.getId());

                // Chi tiêu thực (FolioItem DƯƠNG)
                java.math.BigDecimal charged = amounts.stream()
                        .filter(a -> a != null && a.compareTo(java.math.BigDecimal.ZERO) > 0)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                // Tiền khách đã nạp trước (CHỈ đếm các khoản nạp hạn mức, bỏ tiền cọc Walk-in)
                java.math.BigDecimal deposited = folioItemRepository.findCreditDepositAmountsByDetailId(d.getId())
                        .stream()
                        .filter(a -> a != null && a.compareTo(java.math.BigDecimal.ZERO) < 0)
                        .map(java.math.BigDecimal::abs)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                java.math.BigDecimal limit = d.getSubCreditLimit() != null ? d.getSubCreditLimit()
                        : java.math.BigDecimal.ZERO;
                // Khả dụng = Hạn mức gốc + Đã nạp - Đang chi
                java.math.BigDecimal available = limit.add(deposited).subtract(charged);

                Map<String, Object> roomMap = new java.util.HashMap<>();
                roomMap.put("detailId", d.getId());
                roomMap.put("roomNumber", d.getRoom() != null ? d.getRoom().getRoomNumber() : "N/A");
                roomMap.put("creditLimit", limit);
                roomMap.put("charged", charged);
                roomMap.put("deposited", deposited);
                roomMap.put("available", available);
                rooms.add(roomMap);
            }

            return ResponseEntity.ok(Map.of("success", true, "rooms", rooms));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
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

        List<RoomBookingDetail> allActive = roomBookingDetailRepository.findByRoomBookingId(bookingId);
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

                // Calculate balance for this room (excludes split items by default inside
                // service)
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

                if (!hasRoomCharge && d.getRoomCharge() != null
                        && d.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
                    groupBalance = groupBalance.add(d.getRoomCharge());
                    groupTotalCharges = groupTotalCharges.add(d.getRoomCharge());
                }
            }
        }

        BigDecimal totalPayments = BigDecimal.ZERO;
        BigDecimal deposit = BigDecimal.ZERO;
        try {
            List<PaymentTransaction> payments = paymentService.getPaymentsByBookingId(bookingId);
            if (payments != null) {
                for (PaymentTransaction pt : payments) {
                    if (pt.getStatus() == PaymentStatus.SUCCESS && pt.getAmount() != null) {
                        totalPayments = totalPayments.add(pt.getAmount());
                        if ("ROOM_BOOKING".equalsIgnoreCase(pt.getTransactionType())
                                || "DEPOSIT".equalsIgnoreCase(pt.getTransactionType())) {
                            deposit = deposit.add(pt.getAmount());
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        boolean depositFromBooking = false;
        if (deposit.compareTo(BigDecimal.ZERO) == 0 && detail.getRoomBooking().getDepositAmount() != null) {
            deposit = detail.getRoomBooking().getDepositAmount();
            depositFromBooking = true;
        }

        BigDecimal otherPayments;
        if (depositFromBooking) {
            otherPayments = totalPayments;
            totalPayments = totalPayments.add(deposit);
        } else {
            otherPayments = totalPayments.subtract(deposit);
        }

        if (otherPayments.compareTo(BigDecimal.ZERO) < 0) {
            otherPayments = BigDecimal.ZERO;
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
                "prePaidDeposit", deposit,
                "otherPayments", otherPayments,
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

        // Ưu tiên: primary contact từ room_guests (người đứng đầu phòng lúc check-in)
        com.kawai.models.RoomGuest primaryGuest = roomGuestRepository
                .findByRoomBookingDetailIdAndIsPrimaryContactTrue(roomBookingDetailId)
                .orElse(null);

        if (primaryGuest != null && primaryGuest.getCustomer() != null
                && primaryGuest.getCustomer().getFullName() != null) {
            // Primary contact là customer
            guestName = primaryGuest.getCustomer().getFullName();
        } else if (primaryGuest != null && primaryGuest.getDependent() != null
                && primaryGuest.getDependent().getDependentName() != null) {
            // Primary contact là dependent (người thân được đăng ký lúc check-in)
            guestName = primaryGuest.getDependent().getDependentName();
        } else if (detail.getCustomer() != null) {
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

        if (!hasRoomCharge && detail.getRoomCharge() != null && detail.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> roomMap = new java.util.HashMap<>();
            roomMap.put("id", -detail.getId());
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

        // Adjust individual room balance: if Checked_Out, outstanding balance is 0.
        // If Checked_In, outstanding balance is charges * 1.10 (deposit is applied only
        // at booking level/consolidated invoice).
        if ("Checked_Out".equalsIgnoreCase(detail.getDetailStatus())) {
            currentBalance = BigDecimal.ZERO;
        } else {
            currentBalance = currentBalance.multiply(new BigDecimal("1.10"));
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

        BigDecimal deposit = BigDecimal.ZERO;
        BigDecimal otherPayments = BigDecimal.ZERO;

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
        response.put("prePaidDeposit", deposit);
        response.put("otherPayments", otherPayments);
        response.put("detailStatus", detail.getDetailStatus());

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

    @PostMapping("/room/{roomBookingDetailId}/request-checkout")
    public ResponseEntity<?> requestCheckout(@PathVariable Long roomBookingDetailId) {
        try {
            Optional<RoomBookingDetail> optDetail = roomBookingDetailRepository.findById(roomBookingDetailId);
            if (optDetail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phòng"));
            }
            RoomBookingDetail detail = optDetail.get();
            com.kawai.models.Room room = detail.getRoom();

            if (!"Checked_In".equalsIgnoreCase(detail.getDetailStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Phòng chưa check-in hoặc đã checkout."));
            }

            // Kiểm tra xem đã có task ROOM_CHECK chưa hoàn thành chưa
            boolean hasPendingCheck = housekeepingTaskRepo.findAll().stream()
                    .anyMatch(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                            && "ROOM_CHECK".equals(t.getOperationalType())
                            && !"Completed".equals(t.getStatus()));
            if (hasPendingCheck) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                        "Phòng đang được chờ kiểm tra (ROOM_CHECK) bởi bộ phận Housekeeping."));
            }

            com.kawai.models.Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);
            com.kawai.models.HotelOperation task = new com.kawai.models.HotelOperation();
            task.setRoom(room);
            task.setStaff(staff);
            task.setSupervisor(staff);
            task.setOperationalType("ROOM_CHECK");
            task.setPriority("High");
            task.setStatus("Pending");
            task.setCreatedAt(LocalDateTime.now());
            task.setNotes("Lễ tân yêu cầu kiểm tra phòng để checkout (Minibar & Hỏng hóc).");

            housekeepingTaskRepo.save(task);

            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Đã gửi yêu cầu kiểm tra phòng đến bộ phận Housekeeping."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/booking/{bookingId}/request-group-checkout")
    public ResponseEntity<?> requestGroupCheckout(@PathVariable Long bookingId) {
        try {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
            if (details.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phòng nào trong đơn đặt phòng này."));
            }

            com.kawai.models.Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);
            List<String> createdRooms = new java.util.ArrayList<>();
            List<String> skippedRooms = new java.util.ArrayList<>();

            for (RoomBookingDetail detail : details) {
                if (!"Checked_In".equalsIgnoreCase(detail.getDetailStatus())) {
                    continue;
                }
                com.kawai.models.Room room = detail.getRoom();
                if (room == null) continue;

                // Kiểm tra xem đã có task ROOM_CHECK chưa hoàn thành chưa
                boolean hasPendingCheck = housekeepingTaskRepo.findAll().stream()
                        .anyMatch(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                                && "ROOM_CHECK".equals(t.getOperationalType())
                                && !"Completed".equals(t.getStatus()));
                if (hasPendingCheck) {
                    skippedRooms.add(room.getRoomNumber());
                    continue;
                }

                com.kawai.models.HotelOperation task = new com.kawai.models.HotelOperation();
                task.setRoom(room);
                task.setStaff(staff);
                task.setSupervisor(staff);
                task.setOperationalType("ROOM_CHECK");
                task.setPriority("High");
                task.setStatus("Pending");
                task.setCreatedAt(LocalDateTime.now());
                task.setNotes("Lễ tân yêu cầu kiểm tra phòng để checkout (Minibar & Hỏng hóc) - Yêu cầu cả đoàn.");

                housekeepingTaskRepo.save(task);
                createdRooms.add(room.getRoomNumber());
            }

            if (createdRooms.isEmpty() && skippedRooms.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không có phòng nào đang ở (Checked_In) để kiểm tra."));
            }

            String msg = "";
            if (!createdRooms.isEmpty()) {
                msg += "Đã gửi yêu cầu kiểm tra cho các phòng: " + String.join(", ", createdRooms) + ". ";
            }
            if (!skippedRooms.isEmpty()) {
                msg += "Các phòng đã có yêu cầu trước đó: " + String.join(", ", skippedRooms) + ".";
            }

            return ResponseEntity.ok(Map.of("success", true, "message", msg.trim()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * UC21.3 & UC22.1 - Gom Folio và Tất toán (Có xử lý Payment & Invoice)
     */
    @PostMapping("/room/{roomBookingDetailId}/checkout")
    @org.springframework.transaction.annotation.Transactional
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

            // KIỂM TRA: Đảm bảo task ROOM_CHECK đã hoàn tất
            boolean hasPendingCheck = housekeepingTaskRepo.findAll().stream()
                    .anyMatch(t -> t.getRoom() != null && t.getRoom().getId().equals(detail.getRoom().getId())
                            && "ROOM_CHECK".equals(t.getOperationalType())
                            && !"Completed".equals(t.getStatus()));
            if (hasPendingCheck) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                        "Vui lòng chờ bộ phận Housekeeping hoàn tất kiểm tra phòng (Minibar/Hỏng hóc) trước khi thanh toán và checkout."));
            }
            boolean isGroup = false;
            if (payload != null && payload.containsKey("isGroup")) {
                isGroup = Boolean.TRUE.equals(payload.get("isGroup"));
            }

            BigDecimal finalBalance = BigDecimal.ZERO;
            if (isGroup) {
                if (booking != null) {
                    List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(booking.getId());
                    for (RoomBookingDetail d : details) {
                        if ("Checked_In".equalsIgnoreCase(d.getDetailStatus())
                                || "Checked_Out".equalsIgnoreCase(d.getDetailStatus())
                                || d.getId().equals(roomBookingDetailId)) {
                            // Auto-post Room Charge if it hasn't been posted yet for this room in the group
                            List<FolioItem> groupItems = nightAuditService.getFolioItems(d.getId());
                            boolean hasPostedRoomCharge = false;
                            if (groupItems != null) {
                                for (FolioItem item : groupItems) {
                                    if ("Room".equalsIgnoreCase(item.getSourceDepartment())) {
                                        hasPostedRoomCharge = true;
                                        break;
                                    }
                                }
                            }
                            if (!hasPostedRoomCharge && d.getRoomCharge() != null
                                    && d.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
                                FolioItem item = new FolioItem();
                                item.setRoomBookingDetail(d);
                                item.setBooking(booking);
                                item.setPayerCustomer(booking != null ? booking.getCustomer() : null);
                                item.setSourceDepartment("Room");
                                item.setAmount(d.getRoomCharge());
                                String catName = d.getCategory() != null ? d.getCategory().getCategoryName() : "Room";
                                item.setDescription("Room Charge (Expected) - " + catName);
                                item.setIsSettledSeparately(false);
                                item.setCreatedAt(java.time.LocalDateTime.now());
                                folioItemRepository.save(item);
                            }

                            BigDecimal roomBal = nightAuditService.calculateFolioBalance(d.getId());
                            finalBalance = finalBalance.add(roomBal);
                        }
                    }
                }
                finalBalance = finalBalance.multiply(new BigDecimal("1.10"));
            } else {
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

                BigDecimal roomBal = BigDecimal.ZERO;
                List<FolioItem> allItemsForBal = nightAuditService.getFolioItems(roomBookingDetailId);
                if (allItemsForBal != null) {
                    for (FolioItem item : allItemsForBal) {
                        roomBal = roomBal.add(item.getAmount());
                    }
                }
                finalBalance = roomBal.multiply(new BigDecimal("1.10"));
            }

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

            if (isGroup && booking != null && booking.getDepositAmount() != null) {
                BigDecimal deposit = booking.getDepositAmount();
                if (deposit.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal recDep = BigDecimal.ZERO;
                    try {
                        List<PaymentTransaction> pts = paymentService.getPaymentsByBookingId(booking.getId());
                        if (pts != null) {
                            for (PaymentTransaction pt : pts) {
                                if (pt.getStatus() == PaymentStatus.SUCCESS
                                        && "Deposit".equalsIgnoreCase(pt.getTransactionType())) {
                                    recDep = recDep.add(pt.getAmount());
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

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

            // Validate: khi thanh toán riêng phòng chỉ cần đủ finalBalance của phòng đó
            // khi thanh toán cả đoàn thì mới check bookingOutstanding
            BigDecimal minRequired;
            if (isGroup) {
                BigDecimal bookingOutstanding = booking != null ? getBookingOutstandingBalance(booking) : finalBalance;
                minRequired = finalBalance.compareTo(bookingOutstanding) < 0 ? finalBalance : bookingOutstanding;
            } else {
                minRequired = finalBalance; // chỉ cần đủ tiền phòng này
            }

            // Trường hợp Balance != 0
            if (minRequired.compareTo(BigDecimal.ZERO) > 0) {
                if (paymentAmount.compareTo(minRequired) < 0) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Khách hàng còn dư nợ " + minRequired + ". Số tiền thanh toán chưa đủ."));
                }
            }

            boolean isVnPay = "VNPAY".equalsIgnoreCase(paymentMethod) && paymentAmount.compareTo(BigDecimal.ZERO) > 0;

            // 1 & 2. Thay đổi trạng thái phòng (CHỈ làm khi KHÔNG PHẢI VNPAY VÀ thao tác
            // này là "Hoàn tất Checkout" tức là paymentAmount = 0)
            boolean isCheckoutAction = (!isVnPay && (paymentAmount.compareTo(BigDecimal.ZERO) == 0 || paymentAmount.compareTo(minRequired) >= 0));
            if (isCheckoutAction) {
                if (isGroup) {
                    List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(booking.getId());
                    for (RoomBookingDetail d : details) {
                        if ("Checked_In".equalsIgnoreCase(d.getDetailStatus())) {
                            d.setDetailStatus("Checked_Out");
                            roomBookingDetailRepository.save(d);

                            if (d.getRoomBooking() != null && d.getRoomBooking().getCustomer() != null) {
                                eventPublisher.publishEvent(new com.kawai.events.CustomerCheckedOutEvent(this,
                                        d.getRoomBooking().getCustomer()));
                            }

                            Room room = d.getRoom();
                            if (room != null) {
                                try {
                                    workflowEngineService.triggerEvent("ROOM_CHECKOUT", Map.of(
                                            "room_id", room.getId(),
                                            "booking_id", booking.getId(),
                                            "booking_detail_id", d.getId()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                room.setRoomStatus("Vacant_Dirty");
                                room.setCurrentBookingDetailId(null);
                                roomRepository.save(room);
                            }
                        }
                    }
                } else {
                    detail.setDetailStatus("Checked_Out");
                    roomBookingDetailRepository.save(detail);

                    if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
                        eventPublisher.publishEvent(new com.kawai.events.CustomerCheckedOutEvent(this,
                                detail.getRoomBooking().getCustomer()));
                    }

                    // 2. Thay đổi trạng thái phòng vật lý qua Dirty (hoặc theo cấu hình workflow)
                    Room room = detail.getRoom();
                    if (room != null) {
                        try {
                            workflowEngineService.triggerEvent("ROOM_CHECKOUT", Map.of(
                                    "room_id", room.getId(),
                                    "booking_id", detail.getRoomBooking().getId(),
                                    "booking_detail_id", detail.getId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Luôn đảm bảo set Vacant_Dirty sau checkout
                        room.setRoomStatus("Vacant_Dirty");
                        room.setCurrentBookingDetailId(null);
                        roomRepository.save(room);
                    }
                }

                // Đồng bộ trạng thái Booking sang Checked_Out nếu toàn bộ các phòng đã checkout
                if (booking != null) {
                    List<RoomBookingDetail> allDetails = roomBookingDetailRepository.findByRoomBookingId(booking.getId());
                    boolean allCheckedOut = allDetails.stream()
                            .allMatch(d -> "Checked_Out".equalsIgnoreCase(d.getDetailStatus()));
                    if (allCheckedOut) {
                        booking.setBookingStatus("Checked_Out");
                        roomBookingRepository.save(booking);
                    }
                }
            }

            // 3. Tạo hóa đơn tổng (Consolidated Invoice) hoặc cập nhật nếu đã có
            ConsolidatedInvoice invoice = consolidatedInvoiceRepository.findByBooking_Id(booking.getId())
                    .orElse(new ConsolidatedInvoice());
            if (invoice.getInvoiceNumber() == null) {
                invoice.setInvoiceNumber("INV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                invoice.setBooking(booking);
                invoice.setSubtotalBeforeVat(BigDecimal.ZERO);
                invoice.setVatAmount(BigDecimal.ZERO);
                invoice.setTotalAmount(BigDecimal.ZERO);
            }
            if (appliedPromo != null) {
                invoice.setPromo(appliedPromo);
            }

            BigDecimal newTotal;
            if (isGroup) {
                newTotal = finalBalance;
            } else {
                BigDecimal currentTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
                newTotal = currentTotal.add(finalBalance);
            }

            BigDecimal subtotal = newTotal.divide(new BigDecimal("1.10"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal vat = newTotal.subtract(subtotal);
            invoice.setSubtotalBeforeVat(subtotal);
            invoice.setVatAmount(vat);
            invoice.setTotalAmount(newTotal);

            boolean allCheckedOut = false;
            if (booking != null) {
                final boolean isGroupFinal = isGroup;
                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(booking.getId());
                allCheckedOut = details.stream()
                        .allMatch(d -> "Checked_Out".equalsIgnoreCase(d.getDetailStatus())
                                || d.getId().equals(detail.getId())
                                || (isGroupFinal && "Checked_In".equalsIgnoreCase(d.getDetailStatus())));
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
                String txnRef = isVnPay
                        ? (isGroup ? "FOLIO_GROUP_" + detail.getId() + "_" + System.currentTimeMillis()
                                : "FOLIO_" + detail.getId() + "_" + System.currentTimeMillis())
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

                Map<String, Object> ctx = new java.util.HashMap<>();
                ctx.put("invoice", invoice);
                ctx.put("pdfPath", pdfPath);
                eventPublisher.publishEvent(new com.kawai.events.SystemEmailEvent(this, customerEmail,
                        "Hóa đơn điện tử - HOANIEN", "invoice", ctx));

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
                            if (newPoints >= 10000)
                                newTier = "Platinum";
                            else if (newPoints >= 5000)
                                newTier = "Gold";
                            else if (newPoints >= 1000)
                                newTier = "Silver";

                            com.kawai.models.MembershipTier tierObj = membershipTierRepository.findByTierNameIgnoreCase(newTier).orElse(null);
                            if (tierObj != null) {
                                customer.setMembershipTier(tierObj);
                            }
                            customerRepository.save(customer);
                            System.out.println("[LOYALTY] Khách " + customer.getFullName() + " vừa nhận " + pointsEarned
                                    + " điểm. Tổng: " + newPoints + " (" + (tierObj != null ? tierObj.getTierName() : (customer.getMembershipTier() != null ? customer.getMembershipTier().getTierName() : "Regular")) + ")");
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
            // Room charges (unposted expected charge)
            boolean hasRoomCharge = false;
            try {
                List<FolioItem> items = nightAuditService.getFolioItems(d.getId());
                if (items != null) {
                    for (FolioItem item : items) {
                        if ("Room".equalsIgnoreCase(item.getSourceDepartment())) {
                            hasRoomCharge = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
            if (!hasRoomCharge && d.getRoomCharge() != null && d.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
                totalCharges = totalCharges.add(d.getRoomCharge());
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
        if (booking.getDepositAmount() != null && booking.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalPayments = totalPayments.add(booking.getDepositAmount());
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
            List<RoomBookingDetail> activeDetails = roomBookingDetailRepository
                    .findByDetailStatusIn(java.util.Arrays.asList("Checked_In", "Checked_Out"));

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
                String phoneNumber = "N/A";
                String checkOutDateStr = "N/A";

                if (!details.isEmpty()) {
                    if (details.get(0).getRoomBooking().getCustomer() != null) {
                        bookerName = details.get(0).getRoomBooking().getCustomer().getFullName();
                        phoneNumber = details.get(0).getRoomBooking().getCustomer().getPhone();
                    }
                    if (details.get(0).getRoomBooking().getCheckOutDate() != null) {
                        checkOutDateStr = details.get(0).getRoomBooking().getCheckOutDate().toString();
                    }
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

                    if (!hasRoomCharge && detail.getRoomCharge() != null
                            && detail.getRoomCharge().compareTo(BigDecimal.ZERO) > 0) {
                        groupBalance = groupBalance.add(detail.getRoomCharge());
                        groupTotalCharges = groupTotalCharges.add(detail.getRoomCharge());
                    }
                }

                // Sum all successful payments for this booking
                BigDecimal totalPayments = BigDecimal.ZERO;
                BigDecimal txDeposit = BigDecimal.ZERO;
                try {
                    List<PaymentTransaction> payments = paymentService.getPaymentsByBookingId(bookingId);
                    if (payments != null) {
                        for (PaymentTransaction pt : payments) {
                            if (pt.getStatus() == PaymentStatus.SUCCESS && pt.getAmount() != null) {
                                totalPayments = totalPayments.add(pt.getAmount());
                                if ("ROOM_BOOKING".equalsIgnoreCase(pt.getTransactionType()) || "DEPOSIT".equalsIgnoreCase(pt.getTransactionType())) {
                                    txDeposit = txDeposit.add(pt.getAmount());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }

                if (txDeposit.compareTo(BigDecimal.ZERO) == 0 && !details.isEmpty() && details.get(0).getRoomBooking().getDepositAmount() != null) {
                    totalPayments = totalPayments.add(details.get(0).getRoomBooking().getDepositAmount());
                }

                groupBalance = groupBalance.multiply(new BigDecimal("1.10")).subtract(totalPayments);
                if (groupBalance.compareTo(BigDecimal.ZERO) < 0) {
                    groupBalance = BigDecimal.ZERO;
                }
                groupTotalPayments = groupTotalPayments.add(totalPayments);
                groupTotalCharges = groupTotalCharges.multiply(new BigDecimal("1.10"));

                Map<String, Object> map = new java.util.HashMap<>();
                map.put("bookingId", bookingId);

                map.put("folioNo", "BKG-" + String.format("%04d", bookingId));
                map.put("roomNumber", String.join(", ", roomNumbers));
                map.put("bookerName", bookerName);
                map.put("totalCharges", groupTotalCharges);
                map.put("totalPayments", groupTotalPayments);
                map.put("balance", groupBalance);
                map.put("phoneNumber", phoneNumber);
                map.put("checkOutDate", checkOutDateStr);
                boolean isAllCheckedOut = details.stream()
                        .allMatch(d -> "Checked_Out".equalsIgnoreCase(d.getDetailStatus()));
                map.put("status", isAllCheckedOut ? "Checked_Out" : "Checked_In");

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

    @PostMapping("/{detailId}/deposit")
    public ResponseEntity<?> depositForCreditLimit(@PathVariable Long detailId,
            @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            RoomBookingDetail detail = roomBookingDetailRepository.findById(detailId).orElse(null);
            if (detail == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy phòng!"));
            }

            if (!payload.containsKey("amount") || !payload.containsKey("method")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin nạp tiền!"));
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(payload.get("amount").toString());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Số tiền nạp phải lớn hơn 0!"));
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Định dạng số tiền không hợp lệ!"));
            }

            String method = payload.get("method").toString().toUpperCase();

            if ("CASH".equals(method)) {
                // Tiền mặt: Ghi nhận như FolioItem âm (ký quỹ/ứng trước)
                // → Checkout sẽ tự động cấn trừ: chi tiêu(+) + đã nạp(-) = số thực nợ
                FolioItem deposit = new FolioItem();
                deposit.setRoomBookingDetail(detail);
                deposit.setBooking(detail.getRoomBooking());
                deposit.setSourceDepartment("FRONT_DESK");
                deposit.setAmount(amount.negate()); // Số tiền âm = đã thu tiền mặt
                deposit.setDescription("Nạp tiền nâng hạn mức (Tiền mặt)");
                if (detail.getCustomer() != null) {
                    deposit.setPayerCustomer(detail.getCustomer());
                } else {
                    deposit.setPayerCustomer(detail.getRoomBooking().getCustomer());
                }
                folioItemRepository.save(deposit);

                return ResponseEntity
                        .ok(Map.of("success", true, "message", "Nạp tiền thành công! Hạn mức khả dụng đã tăng!"));
            } else if ("VNPAY".equals(method)) {
                // VNPay: Tạo transaction và trả về link
                PaymentTransaction txn = new PaymentTransaction();
                txn.setBooking(detail.getRoomBooking()); // Reference to booking
                txn.setAmount(amount);
                txn.setStatus(PaymentStatus.PENDING);
                txn.setTransactionType("CREDIT_LIMIT_DEPOSIT");
                txn.setPaymentMethod("VNPAY");
                txn.setTransactionRef("CREDITDEPOSIT_" + detailId + "_" + System.currentTimeMillis());
                txn.setCreatedAt(java.time.LocalDateTime.now());
                paymentTransactionRepository.save(txn);

                String paymentUrl = vnPayService.createPaymentUrlFromTransaction(txn, request.getRemoteAddr());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Chuyển hướng đến VNPay",
                        "paymentUrl", paymentUrl));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Phương thức thanh toán không hỗ trợ!"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}