package com.kawai.controllers.api;

import com.kawai.models.Booking;
import com.kawai.models.Employee;
import com.kawai.models.HotelOperation;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.BookingRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.HotelOperationRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/manager-requests")
public class ManagerRequestApiController {

    @Autowired
    private HotelOperationRepository hotelOperationRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createManagerRequest(@RequestBody Map<String, Object> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Employee currentStaff = employeeRepository.findByAccountUsername(username).orElse(null);

            if (currentStaff == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy thông tin nhân viên"));
            }

            Long bookingId = null;
            if (payload.get("bookingId") != null) {
                bookingId = Long.valueOf(payload.get("bookingId").toString());
            }

            Long roomBookingDetailId = null;
            if (payload.get("roomBookingDetailId") != null) {
                roomBookingDetailId = Long.valueOf(payload.get("roomBookingDetailId").toString());
            }

            String requestType = (String) payload.get("requestType");
            String description = (String) payload.get("description");

            if (requestType == null || requestType.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Loại yêu cầu không được để trống"));
            }

            HotelOperation op = new HotelOperation();
            op.setOperationalType(requestType);
            op.setPriority("High");
            op.setStatus("Pending");
            op.setCreatedAt(LocalDateTime.now());
            op.setStaff(currentStaff);
            
            // Tìm Manager đầu tiên để assign
            Employee manager = employeeRepository.findAll().stream()
                    .filter(e -> e.getAccount() != null && e.getAccount().getRole() != null &&
                            (e.getAccount().getRole().getRoleName().equals("ROLE_MANAGER") || e.getAccount().getRole().getRoleName().equals("ROLE_ADMIN")))
                    .findFirst()
                    .orElse(currentStaff); // Fallback

            op.setSupervisor(manager);

            Room room = null;
            if (roomBookingDetailId != null) {
                Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository.findById(roomBookingDetailId);
                if (detailOpt.isPresent()) {
                    RoomBookingDetail detail = detailOpt.get();
                    room = detail.getRoom();
                    if (detail.getRoomBooking() != null) {
                        bookingId = detail.getRoomBooking().getId();
                    }
                }
            }

            if (bookingId != null) {
                Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
                if (bookingOpt.isPresent()) {
                    Booking booking = bookingOpt.get();
                    if ("Manager_Approval".equals(requestType)) {
                        booking.setBookingStatus("Pending_Approval");
                        bookingRepository.save(booking);
                    }
                    op.setNotes(description + " - Yêu cầu phê duyệt cho booking ID: " + bookingId);
                } else {
                    op.setNotes(description);
                }
            } else {
                op.setNotes(description);
            }

            if (room != null) {
                op.setRoom(room);
            } else {
                // Fallback room
                Room fallbackRoom = roomRepository.findAll().stream().findFirst().orElse(null);
                op.setRoom(fallbackRoom);
            }

            hotelOperationRepository.save(op);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gửi yêu cầu phê duyệt thành công"
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
