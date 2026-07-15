package com.kawai.controllers.api;

import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.models.Employee;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.FolioItem;
import com.kawai.repositories.HousekeepingTaskRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.FolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private com.kawai.services.interfaces.HousekeepingService housekeepingService;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

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

            if (minibarFee.compareTo(BigDecimal.ZERO) > 0) {
                FolioItem item = new FolioItem();
                item.setBooking(detail.getRoomBooking());
                item.setRoomBookingDetail(detail);
                item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                item.setSourceDepartment("Minibar");
                item.setAmount(minibarFee);
                item.setDescription(description);
                item.setCreatedAt(LocalDateTime.now());
                Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);
                item.setCreatedByStaff(staff);
                item.setRevenueCode("FB_MINIBAR");
                folioItemRepository.save(item);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu thông tin Minibar thành công."));
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

            // Ghi nhận Minibar
            if (minibarFee.compareTo(BigDecimal.ZERO) > 0) {
                FolioItem item = new FolioItem();
                item.setBooking(detail.getRoomBooking());
                item.setRoomBookingDetail(detail);
                item.setPayerCustomer(detail.getRoomBooking().getCustomer());
                item.setSourceDepartment("Housekeeping");
                item.setAmount(minibarFee);
                item.setDescription("Phí sử dụng Minibar (Phòng " + room.getRoomNumber() + ")");
                item.setCreatedAt(LocalDateTime.now());
                item.setCreatedByStaff(task.getStaff());
                item.setRevenueCode("FB_MINIBAR");
                folioItemRepository.save(item);
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'OP_RECEPTION_INHOUSE', 'OP_RECEPTION_CHECKIN')")
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

        // Fetch existing pending/inprogress housekeeping tasks for this room
        List<String> hkTypes = List.of("CHECKOUT_CLEAN", "URGENT_CLEAN", "ROOM_CHECK", "GUEST_REQUEST");
        List<HotelOperation> existingTasks = housekeepingTaskRepo.findAll().stream()
                .filter(t -> t.getRoom() != null && t.getRoom().getId().equals(room.getId())
                        && hkTypes.contains(t.getOperationalType())
                        && ("Pending".equals(t.getStatus()) || "InProgress".equals(t.getStatus())))
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
                messagingTemplate.convertAndSend("/topic/operations", Map.of(
                    "message", "Yêu cầu dọn khẩn cấp phòng " + room.getRoomNumber() + "!",
                    "type", "NEW_TASK"
                ));
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
            messagingTemplate.convertAndSend("/topic/operations", Map.of(
                "message", "Yêu cầu dọn khẩn cấp phòng " + room.getRoomNumber() + "!",
                "type", "NEW_TASK"
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity
                .ok(Map.of("success", true, "message", "Yêu cầu dọn khẩn cấp đã gửi cho bộ phận Buồng phòng."));
    }

    @PostMapping("/housekeeping/reception/create-urgent-maintenance")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_RECEPTIONIST', 'OP_RECEPTION_WALKIN', 'OP_RECEPTION_CHECKIN', 'OP_RECEPTION_CHECKOUT', 'OP_RECEPTION_INHOUSE')")
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
            String finalNotes = "[Lễ tân báo khẩn] " + notes;
            com.kawai.models.HotelOperation maintenanceTask = housekeepingService
                    .createMaintenanceRequest(room.getId(), staff.getId(), finalNotes, true);

            // Gửi WebSocket tin nhắn kênh chung để các màn hình auto-reload
            try {
                messagingTemplate.convertAndSend("/topic/operations", Map.of(
                    "message", "Yêu cầu sửa chữa khẩn cấp phòng " + room.getRoomNumber() + "!",
                    "type", "NEW_TASK"
                ));
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
