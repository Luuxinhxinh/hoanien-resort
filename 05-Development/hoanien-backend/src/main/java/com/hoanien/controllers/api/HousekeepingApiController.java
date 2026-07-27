package com.hoanien.controllers.api;

import com.hoanien.models.HotelOperation;
import com.hoanien.models.Room;
import com.hoanien.models.Employee;
import com.hoanien.models.RoomBookingDetail;
import com.hoanien.models.FolioItem;
import com.hoanien.repositories.HousekeepingTaskRepository;
import com.hoanien.repositories.RoomRepository;
import com.hoanien.repositories.EmployeeRepository;
import com.hoanien.repositories.RoomBookingDetailRepository;
import com.hoanien.repositories.FolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class HousekeepingApiController {

    @Autowired
    private HousekeepingTaskRepository housekeepingTaskRepo;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private FolioItemRepository folioItemRepository;

    @Autowired
    private com.hoanien.services.interfaces.HousekeepingService housekeepingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/housekeeping/save-minibar-only")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'OP_HOUSEKEEPING', 'ROLE_HOUSEKEEPING')")
    public ResponseEntity<?> saveMinibarOnly(@RequestBody Map<String, Object> request) {
        try {
            Long roomId = Long.valueOf(request.get("roomId").toString());
            BigDecimal minibarFee = new BigDecimal(request.getOrDefault("minibarFee", "0").toString());
            String description = (String) request.getOrDefault("description", "Phí sử dụng Minibar");

            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phòng không tồn tại."));
            }

            Long detailId = room.getCurrentBookingDetailId();
            if (detailId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Phòng không có booking active."));
            }

            RoomBookingDetail detail = roomBookingDetailRepository.findById(detailId).orElse(null);
            if (detail == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy booking detail."));
            }

            Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);

            // Xóa các khoản Minibar chưa thanh toán cũ của phòng này (Ghi đè thông tin mới)
            List<FolioItem> oldMinibarItems = folioItemRepository.findByRoomBookingDetail(detail).stream()
                    .filter(fi -> "FB_MINIBAR".equalsIgnoreCase(fi.getRevenueCode())
                            || "Minibar".equalsIgnoreCase(fi.getSourceDepartment())
                            || (fi.getDescription() != null && fi.getDescription().toLowerCase().contains("minibar")))
                    .filter(fi -> Boolean.FALSE.equals(fi.getIsSettledSeparately()))
                    .collect(Collectors.toList());
            if (!oldMinibarItems.isEmpty()) {
                folioItemRepository.deleteAll(oldMinibarItems);
                folioItemRepository.flush();
            }

            Object rawItems = request.get("items");
            if (rawItems instanceof List && !((List<?>) rawItems).isEmpty()) {
                List<?> itemList = (List<?>) rawItems;
                for (Object rawItem : itemList) {
                    if (rawItem instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) rawItem;
                        String name = itemMap.get("name") != null ? itemMap.get("name").toString() : "Đồ minibar";
                        int qty = itemMap.get("qty") != null ? Integer.parseInt(itemMap.get("qty").toString()) : 1;
                        BigDecimal amt = itemMap.get("amount") != null
                                ? new BigDecimal(itemMap.get("amount").toString())
                                : BigDecimal.ZERO;
                        if (qty > 0) {
                            FolioItem item = new FolioItem();
                            item.setBooking(detail.getRoomBooking());
                            item.setRoomBookingDetail(detail);
                            item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                            item.setSourceDepartment("Minibar");
                            item.setAmount(amt);
                            item.setDescription("Phí sử dụng Minibar: " + name + " (x" + qty + ")");
                            item.setCreatedAt(LocalDateTime.now());
                            item.setCreatedByStaff(staff);
                            item.setRevenueCode("FB_MINIBAR");
                            folioItemRepository.save(item);
                        }
                    }
                }
            } else if (minibarFee.compareTo(BigDecimal.ZERO) > 0) {
                FolioItem item = new FolioItem();
                item.setBooking(detail.getRoomBooking());
                item.setRoomBookingDetail(detail);
                item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                item.setSourceDepartment("Minibar");
                item.setAmount(minibarFee);
                String descStr = description;
                if (!descStr.toLowerCase().startsWith("phí sử dụng minibar:")) {
                    descStr = "Phí sử dụng Minibar: " + descStr;
                }
                item.setDescription(descStr);
                item.setCreatedAt(LocalDateTime.now());
                item.setCreatedByStaff(staff);
                item.setRevenueCode("FB_MINIBAR");
                folioItemRepository.save(item);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu thông tin Minibar thành công."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/housekeeping/get-minibar-items")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'OP_HOUSEKEEPING', 'ROLE_HOUSEKEEPING')")
    public ResponseEntity<?> getMinibarItems(@RequestParam Long roomId) {
        try {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null || room.getCurrentBookingDetailId() == null) {
                return ResponseEntity.ok(Map.of("success", true, "items", Map.of("evian", 0, "coca", 0, "heineken", 0, "snack", 0, "toothbrush", 0, "showergel", 0, "shampoo", 0, "towel", 0)));
            }
            RoomBookingDetail detail = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId()).orElse(null);
            if (detail == null) {
                return ResponseEntity.ok(Map.of("success", true, "items", Map.of("evian", 0, "coca", 0, "heineken", 0, "snack", 0, "toothbrush", 0, "showergel", 0, "shampoo", 0, "towel", 0)));
            }

            List<FolioItem> oldMinibarItems = folioItemRepository.findByRoomBookingDetail(detail).stream()
                    .filter(fi -> "FB_MINIBAR".equalsIgnoreCase(fi.getRevenueCode())
                            || "Minibar".equalsIgnoreCase(fi.getSourceDepartment())
                            || (fi.getDescription() != null && fi.getDescription().toLowerCase().contains("minibar")))
                    .filter(fi -> Boolean.FALSE.equals(fi.getIsSettledSeparately()))
                    .collect(Collectors.toList());

            int evian = 0, coca = 0, heineken = 0, snack = 0;
            int toothbrush = 0, showergel = 0, shampoo = 0, towel = 0;
            for (FolioItem item : oldMinibarItems) {
                String desc = item.getDescription();
                if (desc == null) continue;
                String[] tokens = desc.split("[,:]");
                for (String token : tokens) {
                    String lower = token.toLowerCase();
                    int qty = 1;
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(x(\\d+)\\)").matcher(token);
                    if (m.find()) {
                        qty = Integer.parseInt(m.group(1));
                    }
                    if (lower.contains("evian")) {
                        evian += qty;
                    } else if (lower.contains("coca")) {
                        coca += qty;
                    } else if (lower.contains("heineken")) {
                        heineken += qty;
                    } else if (lower.contains("snack")) {
                        snack += qty;
                    } else if (lower.contains("bàn chải") || lower.contains("toothbrush")) {
                        toothbrush += qty;
                    } else if (lower.contains("sữa tắm") || lower.contains("showergel")) {
                        showergel += qty;
                    } else if (lower.contains("dầu gội") || lower.contains("shampoo")) {
                        shampoo += qty;
                    } else if (lower.contains("khăn tắm") || lower.contains("towel")) {
                        towel += qty;
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "items", Map.of(
                "evian", evian,
                "coca", coca,
                "heineken", heineken,
                "snack", snack,
                "toothbrush", toothbrush,
                "showergel", showergel,
                "shampoo", shampoo,
                "towel", towel
            )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/housekeeping/complete-room-check")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'OP_HOUSEKEEPING')")
    public ResponseEntity<?> completeRoomCheck(@RequestBody Map<String, Object> request) {
        try {
            Long taskId = Long.valueOf(request.get("taskId").toString());
            BigDecimal minibarFee = new BigDecimal(request.getOrDefault("minibarFee", "0").toString());
            BigDecimal damageFee = new BigDecimal(request.getOrDefault("damageFee", "0").toString());
            String notes = (String) request.getOrDefault("notes", "");
            Object rawItems = request.get("items");

            HotelOperation task = housekeepingTaskRepo.findById(taskId).orElse(null);
            if (task == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy task."));
            }

            Room room = task.getRoom();
            Long detailId = room.getCurrentBookingDetailId();
            if (detailId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Phòng không có booking active."));
            }

            RoomBookingDetail detail = roomBookingDetailRepository.findById(detailId).orElse(null);
            if (detail == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy booking detail."));
            }

            // Ghi nhận Minibar (Chỉ cập nhật/ghi đè nếu có thông tin minibar mới gửi lên)
            boolean hasNewMinibarData = (rawItems instanceof List && !((List<?>) rawItems).isEmpty())
                    || minibarFee.compareTo(BigDecimal.ZERO) > 0;

            if (hasNewMinibarData) {
                List<FolioItem> oldMinibarItems = folioItemRepository.findByRoomBookingDetail(detail).stream()
                        .filter(fi -> "FB_MINIBAR".equalsIgnoreCase(fi.getRevenueCode())
                                || "Minibar".equalsIgnoreCase(fi.getSourceDepartment())
                                || (fi.getDescription() != null && fi.getDescription().toLowerCase().contains("minibar")))
                        .filter(fi -> Boolean.FALSE.equals(fi.getIsSettledSeparately()))
                        .collect(Collectors.toList());
                if (!oldMinibarItems.isEmpty()) {
                    folioItemRepository.deleteAll(oldMinibarItems);
                    folioItemRepository.flush();
                }

                if (rawItems instanceof List && !((List<?>) rawItems).isEmpty()) {
                    List<?> itemList = (List<?>) rawItems;
                    for (Object rawItem : itemList) {
                        if (rawItem instanceof Map) {
                            Map<?, ?> itemMap = (Map<?, ?>) rawItem;
                            String name = itemMap.get("name") != null ? itemMap.get("name").toString() : "Đồ minibar";
                            int qty = itemMap.get("qty") != null ? Integer.parseInt(itemMap.get("qty").toString()) : 1;
                            BigDecimal amt = itemMap.get("amount") != null
                                    ? new BigDecimal(itemMap.get("amount").toString())
                                    : BigDecimal.ZERO;
                            if (amt.compareTo(BigDecimal.ZERO) > 0) {
                                FolioItem item = new FolioItem();
                                item.setBooking(detail.getRoomBooking());
                                item.setRoomBookingDetail(detail);
                                item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                                item.setSourceDepartment("Minibar");
                                item.setAmount(amt);
                                item.setDescription("Phí sử dụng Minibar: " + name + " (x" + qty + ")");
                                item.setCreatedAt(LocalDateTime.now());
                                item.setCreatedByStaff(task.getStaff());
                                item.setRevenueCode("FB_MINIBAR");
                                folioItemRepository.save(item);
                            }
                        }
                    }
                } else if (minibarFee.compareTo(BigDecimal.ZERO) > 0) {
                    FolioItem item = new FolioItem();
                    item.setBooking(detail.getRoomBooking());
                    item.setRoomBookingDetail(detail);
                    item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                    item.setSourceDepartment("Minibar");
                    item.setAmount(minibarFee);
                    String descStr = (String) request.getOrDefault("description",
                            "Phí sử dụng Minibar: Sử dụng đồ minibar (Phòng " + room.getRoomNumber() + ")");
                    if (!descStr.toLowerCase().startsWith("phí sử dụng minibar:")) {
                        descStr = "Phí sử dụng Minibar: " + descStr;
                    }
                    item.setDescription(descStr);
                    item.setCreatedAt(LocalDateTime.now());
                    item.setCreatedByStaff(task.getStaff());
                    item.setRevenueCode("FB_MINIBAR");
                    folioItemRepository.save(item);
                }
            }

            // Ghi nhận Hỏng hóc
            if (damageFee.compareTo(BigDecimal.ZERO) > 0) {
                FolioItem item = new FolioItem();
                item.setBooking(detail.getRoomBooking());
                item.setRoomBookingDetail(detail);
                item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                item.setSourceDepartment("Housekeeping");
                item.setAmount(damageFee);
                item.setDescription("Phí đền bù hỏng hóc (Phòng " + room.getRoomNumber() + ")");
                item.setCreatedAt(LocalDateTime.now());
                item.setCreatedByStaff(task.getStaff());
                item.setRevenueCode("OTH_MISC");
                folioItemRepository.save(item);
            }

            task.setStatus("Completed");
            task.setCompletedAt(LocalDateTime.now());
            task.setNotes(task.getNotes() + " | Hoàn tất kiểm tra phòng: " + notes);
            housekeepingTaskRepo.save(task);

            return ResponseEntity.ok(Map.of("success", true, "message", "Hoàn tất kiểm tra phòng thành công."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/housekeeping/reception/escalate-task")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'OP_RECEPTION_INHOUSE', 'OP_RECEPTION_CHECKIN')")
    public ResponseEntity<?> escalateTask(@RequestBody Map<String, String> request) {
        String roomNumber = request.get("roomNumber");
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng cung cấp số phòng."));
        }

        Room room = roomRepository.findByRoomNumber(roomNumber).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phòng không tồn tại."));
        }

        Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);
        if (staff == null) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Lỗi cấu hình hệ thống (không có nhân viên)."));
        }

        // Fetch existing pending/inprogress cleaning tasks for this room (exclude
        // maintenance)
        List<HotelOperation> existingTasks = housekeepingTaskRepo.findAll().stream()
                .filter(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                        && ("Pending".equals(t.getStatus()) || "InProgress".equals(t.getStatus()))
                        && !"MAINTENANCE".equalsIgnoreCase(t.getOperationalType())
                        && !"URGENT_MAINTENANCE".equalsIgnoreCase(t.getOperationalType()))
                .toList();

        if (!existingTasks.isEmpty()) {
            for (HotelOperation t : existingTasks) {
                t.setPriority("Lễ tân báo dọn khẩn");
                t.setOperationalType("URGENT_CLEAN");
                String currentNotes = t.getNotes() != null ? t.getNotes() : "";
                t.setNotes(currentNotes + " \n[Khẩn cấp] Lễ tân hối thúc dọn ưu tiên để khách Check-in!");
                housekeepingTaskRepo.save(t);
            }
            try {
                messagingTemplate.convertAndSend("/topic/operations/housekeeping", Map.of(
                        "message", "Yêu cầu dọn khẩn cấp phòng " + room.getRoomNumber() + "!",
                        "type", "NEW_TASK"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Đã nâng độ ưu tiên dọn khẩn cấp cho phòng này."));
        }

        HotelOperation task = new HotelOperation();
        task.setRoom(room);
        task.setStaff(staff);
        task.setSupervisor(staff);
        task.setOperationalType("URGENT_CLEAN");
        task.setPriority("Lễ tân báo dọn khẩn");
        task.setStatus("Pending");
        task.setCreatedAt(LocalDateTime.now());
        task.setNotes("Lễ tân yêu cầu dọn phòng khẩn cấp.");

        housekeepingTaskRepo.save(task);
        try {
            messagingTemplate.convertAndSend("/topic/operations/housekeeping", Map.of(
                    "message", "Yêu cầu dọn khẩn cấp phòng " + room.getRoomNumber() + "!",
                    "type", "NEW_TASK"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity
                .ok(Map.of("success", true, "message", "Yêu cầu dọn khẩn cấp đã gửi cho bộ phận Buồng phòng."));
    }

    @PostMapping("/housekeeping/reception/create-urgent-maintenance")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'OP_RECEPTION_WALKIN', 'OP_RECEPTION_CHECKIN', 'OP_RECEPTION_CHECKOUT', 'OP_RECEPTION_INHOUSE')")
    public ResponseEntity<?> createUrgentMaintenance(@RequestBody Map<String, String> request) {
        String roomNumber = request.get("roomNumber");
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng cung cấp số phòng."));
        }

        Room room = roomRepository.findByRoomNumber(roomNumber).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phòng không tồn tại."));
        }

        Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);
        if (staff == null) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Lỗi cấu hình hệ thống (không có nhân viên)."));
        }
        try {
            String notes = request.get("notes");
            if (notes == null || notes.trim().isEmpty()) {
                notes = "Lễ tân báo hỏng hóc khẩn cấp cần sửa chữa ngay.";
            }
            com.hoanien.models.HotelOperation maintenanceTask = housekeepingService
                    .createMaintenanceRequest(room.getId(), staff.getId(), notes, true);

            // Gửi WebSocket tin nhắn kênh riêng bảo trì để màn hình bảo trì nhận và
            // auto-reload
            try {
                messagingTemplate.convertAndSend("/topic/operations/maintenance", Map.of(
                        "message", "Yêu cầu sửa chữa khẩn cấp phòng " + room.getRoomNumber() + "!",
                        "type", "NEW_TASK"));
            } catch (Exception wsEx) {
                wsEx.printStackTrace();
            }
            return ResponseEntity.ok(Map.of("success", true, "message",
                    "Đã gửi yêu cầu sửa chữa khẩn cấp cho phòng " + roomNumber + "."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
