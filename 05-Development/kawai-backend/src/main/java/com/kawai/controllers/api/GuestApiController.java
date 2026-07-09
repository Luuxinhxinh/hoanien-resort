package com.kawai.controllers.api;

import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.models.Employee;
import com.kawai.repositories.HousekeepingTaskRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/guest")
public class GuestApiController {

    @Autowired
    private HousekeepingTaskRepository housekeepingTaskRepo;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/request-clean")
    public ResponseEntity<?> requestClean(@RequestBody Map<String, String> request) {
        String roomNumber = request.get("roomNumber");
        String notes = request.get("notes");

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng cung cấp số phòng."));
        }

        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phòng không tồn tại."));
        }

        Room room = roomOpt.get();

        // Lấy 1 nhân viên tạm thời để gán cho task (vì DB yêu cầu staff_id)
        Employee staff = employeeRepository.findAll().stream().findFirst().orElse(null);
        if (staff == null) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi cấu hình hệ thống (không có nhân viên)."));
        }

        HotelOperation task = new HotelOperation();
        task.setRoom(room);
        task.setStaff(staff);
        task.setSupervisor(staff);
        task.setOperationalType("GUEST_REQUEST");
        task.setPriority("High");
        task.setStatus("Pending");
        task.setCreatedAt(LocalDateTime.now());
        String noteToSave = (notes != null && !notes.trim().isEmpty()) ? notes.trim() : "Khách yêu cầu dọn phòng.";
        task.setNotes(noteToSave);

        housekeepingTaskRepo.save(task);

        return ResponseEntity.ok(Map.of("success", true, "message", "Yêu cầu dọn phòng đã được gửi thành công."));
    }
}
