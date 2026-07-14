package com.kawai.controllers.api;

import com.kawai.models.Employee;
import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.HotelOperationRepository;
import com.kawai.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/guest-requests")
public class GuestRequestApiController {

    @Autowired
    private HotelOperationRepository hotelOperationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createGuestRequest(@RequestBody Map<String, Object> payload) {
        try {
            Long roomId = null;
            if (payload.get("roomId") != null) {
                roomId = Long.valueOf(payload.get("roomId").toString());
            }

            if (roomId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin phòng"));
            }

            String requestType = (String) payload.get("requestType");
            String description = (String) payload.get("description");

            if (requestType == null || requestType.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Loại yêu cầu không được để trống"));
            }

            Optional<Room> roomOpt = roomRepository.findById(roomId);
            if (!roomOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy phòng"));
            }

            Room room = roomOpt.get();

            // Tìm một nhân viên admin/manager làm người tiếp nhận ban đầu (fallback)
            Employee defaultStaff = employeeRepository.findAll().stream()
                    .filter(e -> e.getAccount() != null && e.getAccount().getRole() != null &&
                            (e.getAccount().getRole().getRoleName().equals("ROLE_ADMIN") || e.getAccount().getRole().getRoleName().equals("ROLE_MANAGER")))
                    .findFirst()
                    .orElse(employeeRepository.findAll().stream().findFirst().orElse(null));

            if (defaultStaff == null) {
                return ResponseEntity.internalServerError().body(Map.of("message", "Hệ thống chưa có nhân viên nào để tiếp nhận"));
            }

            HotelOperation op = new HotelOperation();
            op.setOperationalType(requestType); // EXPECTED: "GUEST_REQUEST" hoặc "MAINTENANCE"
            op.setPriority("High");
            op.setStatus("Pending");
            op.setCreatedAt(LocalDateTime.now());
            op.setRoom(room);
            op.setStaff(defaultStaff);
            op.setSupervisor(defaultStaff);

            String prefix = requestType.equals("GUEST_REQUEST") ? "[Khách Yêu Cầu Dọn Phòng]" : "[Khách Yêu Cầu Sửa Chữa]";
            String defaultDesc = requestType.equals("GUEST_REQUEST") ? "Yêu cầu dọn dẹp phòng" : "Yêu cầu sửa chữa thiết bị";
            String desc = (description != null && !description.trim().isEmpty()) ? description.trim() : defaultDesc;
            op.setNotes(prefix + " - " + desc);

            hotelOperationRepository.save(op);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gửi yêu cầu thành công! Nhân viên sẽ đến hỗ trợ bạn trong ít phút."
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tạo yêu cầu: " + e.getMessage()
            ));
        }
    }
}
