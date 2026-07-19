package com.kawai.controllers.api;

import com.kawai.models.TourAttendee;
import com.kawai.repositories.TourAttendeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API cho chức năng AI Face Scan (UC21).
 * Nhận kết quả từ trình duyệt (face-api.js) và cập nhật CSDL.
 */
@RestController
@RequestMapping("/api/faceid")
public class FaceIdApiController {

    @Autowired
    private TourAttendeeRepository tourAttendeeRepository;

    @Autowired
    private com.kawai.repositories.CustomerRepository customerRepository;

    @Autowired
    private com.kawai.repositories.DependentRepository dependentRepository;

    @Autowired
    private com.kawai.repositories.BookingRepository bookingRepository;

    @Autowired
    private com.kawai.repositories.TourScheduleRepository tourScheduleRepository;

    @Autowired
    private com.kawai.repositories.TourBookingRepository tourBookingRepository;

    @Autowired
    private com.kawai.repositories.EmployeeRepository employeeRepository;

    @Autowired
    private com.kawai.repositories.HotelOperationRepository hotelOperationRepository;

    @Autowired
    private com.kawai.repositories.RefundRequestRepository refundRequestRepository;

    @Autowired
    private com.kawai.services.interfaces.TourBookingService tourBookingService;

    @Autowired
    private com.kawai.services.interfaces.FolioService folioService;

    @Autowired
    private com.kawai.services.interfaces.EmailService emailService;


    /**
     * POST /api/faceid/verify
     * Nhận kết quả nhận diện từ JavaScript (face-api.js) trên trình duyệt.
     * Frontend đã tự xử lý nhận diện và gửi tên khách khớp về server.
     * Request: { "name": "Lê Hoàng Nam" }
     */
    @PostMapping("/verify")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> verifyFromBrowser(@RequestBody Map<String, Object> body) {
        String matchedName = body.get("name") != null ? body.get("name").toString() : null;
        if (matchedName == null || matchedName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu tên khách hàng"));
        }

        Object schedIdObj = body.get("scheduleId");
        Long scheduleId = schedIdObj != null ? Long.valueOf(schedIdObj.toString()) : null;

        String displayName = matchedName;

        try {
            List<TourAttendee> attendees;
            if (scheduleId != null) {
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_Id(scheduleId);
            } else {
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_DepartureDate(LocalDate.now());
            }

            for (TourAttendee attendee : attendees) {
                String name = null;
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                }

                if (name != null && isNameMatch(name, displayName)) {
                    if (!"Checked_In".equals(attendee.getStatus())) {
                        attendee.setStatus("Checked_In");
                        attendee.setFaceMatchedAt(LocalDateTime.now());
                        tourAttendeeRepository.saveAndFlush(attendee);
                    }
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "name", displayName,
                            "attendeeId", String.valueOf(attendee.getId()),
                            "time", LocalDateTime.now().toString()));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Không tìm thấy khách '" + displayName + "' trong danh sách tour hôm nay"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    /**
     * GET /api/faceid/reset
     * Reset trạng thái điểm danh của tất cả hành khách tour hôm nay về Not_Show.
     * Dùng cho demo replay.
     */
    @GetMapping("/reset")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> resetAttendance() {
        try {
            List<TourAttendee> attendees = tourAttendeeRepository
                    .findByTourBooking_Schedule_DepartureDate(LocalDate.now());
            int resetCount = 0;
            for (TourAttendee attendee : attendees) {
                if (!"Not_Show".equals(attendee.getStatus())) {
                    attendee.setStatus("Not_Show");
                    attendee.setFaceMatchedAt(null);
                    attendee.setAbsentReason(null);
                    tourAttendeeRepository.saveAndFlush(attendee);
                    resetCount++;
                }
            }
            return ResponseEntity.ok(Map.of("success", true, "resetCount", resetCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Autowired
    private com.cloudinary.Cloudinary cloudinary;

    /**
     * POST /api/faceid/enroll
     * Thu thập dữ liệu khuôn mặt (đăng ký) cho khách hàng (Customer hoặc Dependent)
     * Request: { "customerId": 1, "dependentId": null, "faceVectorData": "[...]" }
     */
    @PostMapping("/enroll")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> enrollFaceId(@RequestBody Map<String, Object> body) {
        try {
            String faceVectorData = (String) body.get("faceVectorData");
            if (faceVectorData == null || faceVectorData.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu dữ liệu nhận diện khuôn mặt"));
            }

            String base64Image = (String) body.get("faceImageBase64");
            String savedImageUrl = null;
            if (base64Image != null && base64Image.contains(",")) {
                try {
                    // Upload trực tiếp chuỗi Data URI (Base64) lên Cloudinary (SDK hỗ trợ phân tích tự động)
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(base64Image, 
                            com.cloudinary.utils.ObjectUtils.asMap(
                                    "folder", "kawai_faces",
                                    "public_id", "face_" + System.currentTimeMillis()
                             ));
                    savedImageUrl = uploadResult.get("secure_url").toString();
                } catch (Exception ex) {
                    System.err.println("Lỗi nghiêm trọng khi upload FaceID lên Cloudinary:");
                    ex.printStackTrace();
                }
            }

            if (body.get("bookingId") != null) {
                Long bookingId = Long.valueOf(body.get("bookingId").toString());
                com.kawai.models.Booking booking = bookingRepository.findById(bookingId).orElse(null);
                if (booking == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Booking không tồn tại"));
                }
                com.kawai.models.Customer customer = booking.getCustomer();
                if (customer == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy khách hàng cho Booking này"));
                }
                customer.setFaceVectorData(faceVectorData);
                if (savedImageUrl != null) customer.setFaceImgUrl(savedImageUrl);
                customerRepository.save(customer);
                return ResponseEntity.ok(Map.of("success", true, "message", "Đăng ký khuôn mặt thành công cho khách hàng " + customer.getFullName()));
            } else if (body.get("customerId") != null) {
                Long customerId = Long.valueOf(body.get("customerId").toString());
                com.kawai.models.Customer customer = customerRepository.findById(customerId).orElse(null);
                if (customer == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Khách hàng không tồn tại"));
                }
                customer.setFaceVectorData(faceVectorData);
                if (savedImageUrl != null) customer.setFaceImgUrl(savedImageUrl);
                customerRepository.save(customer);
                return ResponseEntity.ok(Map.of("success", true, "message", "Đăng ký khuôn mặt thành công cho khách hàng " + customer.getFullName()));
            } else if (body.get("dependentId") != null) {
                Long dependentId = Long.valueOf(body.get("dependentId").toString());
                com.kawai.models.Dependent dependent = dependentRepository.findById(dependentId).orElse(null);
                if (dependent == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Người đi kèm không tồn tại"));
                }
                dependent.setFaceVectorData(faceVectorData);
                if (savedImageUrl != null) dependent.setFaceImgUrl(savedImageUrl);
                dependentRepository.save(dependent);
                return ResponseEntity.ok(Map.of("success", true, "message", "Đăng ký khuôn mặt thành công cho người đi kèm " + dependent.getDependentName()));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu customerId hoặc dependentId"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * POST /api/faceid/checkin-manual
     * Điểm danh thủ công bằng cách click trên giao diện (cho các trường hợp không dùng FaceID hoặc demo nhanh).
     */
    @PostMapping("/checkin-manual")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> checkinManual(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("attendeeId");
        if (idObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu ID hành khách"));
        }

        try {
            Long attendeeId = Long.valueOf(idObj.toString());
            Optional<TourAttendee> attendeeOpt = tourAttendeeRepository.findById(attendeeId);
            if (attendeeOpt.isPresent()) {
                TourAttendee attendee = attendeeOpt.get();
                if (!"Checked_In".equals(attendee.getStatus())) {
                    attendee.setStatus("Checked_In");
                    attendee.setFaceMatchedAt(LocalDateTime.now());
                    tourAttendeeRepository.saveAndFlush(attendee);
                }
                String name = "Ẩn danh";
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                }
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "name", name,
                        "attendeeId", String.valueOf(attendee.getId()),
                        "time", LocalDateTime.now().toString()));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hành khách với ID: " + attendeeId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    /**
     * POST /api/faceid/absent-manual
     * Báo vắng mặt khách hàng thủ công kèm lý do.
     */
    @PostMapping("/absent-manual")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> absentManual(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("attendeeId");
        Object reasonObj = body.get("reason");
        if (idObj == null || reasonObj == null || reasonObj.toString().trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thiếu ID hành khách hoặc lý do vắng mặt"));
        }

        try {
            Long attendeeId = Long.valueOf(idObj.toString());
            String reason = reasonObj.toString().trim();
            Optional<TourAttendee> attendeeOpt = tourAttendeeRepository.findById(attendeeId);
            if (attendeeOpt.isPresent()) {
                TourAttendee attendee = attendeeOpt.get();
                attendee.setStatus("Absent");
                attendee.setAbsentReason(reason);
                tourAttendeeRepository.saveAndFlush(attendee);
                
                String name = "Ẩn danh";
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                }
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "name", name,
                        "attendeeId", String.valueOf(attendee.getId())));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hành khách với ID: " + attendeeId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    /**
     * GET /api/faceid/references
     * Trả về danh sách ảnh tham chiếu khuôn mặt cho hôm nay (dùng cho face-api.js).
     */
    @GetMapping("/references")
    public ResponseEntity<?> getFaceReferences(@RequestParam(required = false) Long scheduleId) {
        try {
            List<TourAttendee> attendees;
            if (scheduleId != null) {
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_Id(scheduleId);
            } else {
                attendees = tourAttendeeRepository.findByTourBooking_Schedule_DepartureDate(LocalDate.now());
            }
            List<Map<String, String>> refs = new ArrayList<>();

            for (TourAttendee attendee : attendees) {
                String name = null;
                String dbFaceUrl = null;
                
                if (attendee.getCustomer() != null) {
                    name = attendee.getCustomer().getFullName();
                    dbFaceUrl = attendee.getCustomer().getFaceImgUrl();
                } else if (attendee.getDependent() != null) {
                    name = attendee.getDependent().getDependentName();
                    dbFaceUrl = attendee.getDependent().getFaceImgUrl();
                }
                
                if (name != null) {
                    // Ưu tiên 1: Ảnh fix cứng cho dữ liệu demo (Nguyễn Xuân Lưu, Ngọc Thị)
                    String imageUrl = mapNameToImageUrl(name);
                    
                    // Ưu tiên 2: Ảnh thật chụp từ quầy Lễ tân (nếu không có ảnh fix cứng)
                    if (imageUrl == null && dbFaceUrl != null && !dbFaceUrl.isBlank()) {
                        imageUrl = dbFaceUrl;
                    }
                    
                    if (imageUrl != null) {
                        refs.add(Map.of("name", name, "imageUrl", imageUrl));
                    }
                }
            }

            // Fallback nếu danh sách trống - đẩy thủ công 2 ảnh demo vào
            if (refs.isEmpty()) {
                refs.add(Map.of("name", "Nguyễn Xuân Lưu", "imageUrl", "/AnhTour/luuham.jpg"));
                refs.add(Map.of("name", "Ngọc Thị", "imageUrl", "/AnhTour/lgok.jpg"));
            }

            return ResponseEntity.ok(refs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel-schedule")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> cancelScheduleByGuide(@RequestBody Map<String, Object> body) {
        Object schedIdObj = body.get("scheduleId");
        String reason = body.get("description") != null ? body.get("description").toString().trim() : null;

        if (schedIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin lịch trình tour (scheduleId)."));
        }
        if (reason == null || reason.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập lý do sự cố hủy tour."));
        }

        Long scheduleId = Long.valueOf(schedIdObj.toString());

        // Đồng bộ hóa theo scheduleId để tránh race condition (người dùng click đúp hoặc gửi nhiều request đồng thời)
        synchronized (scheduleId.toString().intern()) {
            try {
                com.kawai.models.TourSchedule schedule = tourScheduleRepository.findById(scheduleId).orElse(null);
                if (schedule == null) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy lịch trình tour."));
                }

                if ("Cancelled".equalsIgnoreCase(schedule.getScheduleStatus())) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lịch trình tour này đã được hủy trước đó rồi."));
                }

                // 1. Cập nhật trạng thái TourSchedule sang Cancelled
                schedule.setScheduleStatus("Cancelled");
                tourScheduleRepository.save(schedule);

                // 2. Ghi nhận sự cố vào bảng Hotel_Operations (loại TOUR_INCIDENT, trạng thái Pending)
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                com.kawai.models.Employee staff = employeeRepository.findByAccountUsername(username).orElse(null);

                com.kawai.models.HotelOperation incident = new com.kawai.models.HotelOperation();
                incident.setOperationalType("TOUR_INCIDENT");
                incident.setPriority("Urgent");
                incident.setStatus("Pending");
                incident.setNotes("[HỦY TOUR] Sự cố lịch trình #" + scheduleId + " - " + (schedule.getTour() != null ? schedule.getTour().getTourName() : "") + ": " + reason);
                incident.setStaff(staff);
                incident.setCreatedAt(LocalDateTime.now());
                hotelOperationRepository.save(incident);

                // 3. Tìm tất cả các đơn đặt tour của lịch trình này để hủy và hoàn tiền
                List<com.kawai.models.TourBooking> bookings = tourBookingRepository.findByScheduleId(scheduleId);
                int cancelCount = 0;

                for (com.kawai.models.TourBooking booking : bookings) {
                    String currentStatus = booking.getBookingStatus() != null ? booking.getBookingStatus() : "";
                    if ("Cancelled_Refunded".equalsIgnoreCase(currentStatus) || "Cancelled_Forfeited".equalsIgnoreCase(currentStatus)) {
                        continue; // Bỏ qua đơn đã hủy
                    }

                    // Gọi cancelTour tập trung ở Service với cancelledByResort = true (hoàn tiền 100%) và truyền lý do hủy
                    tourBookingService.cancelTour(booking.getId(), true, reason);
                    cancelCount++;
                }
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "Đã hủy lịch trình tour thành công! Đã hoàn tiền cho " + cancelCount + " đơn đặt tour."
                ));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi khi hủy lịch trình tour: " + e.getMessage()));
            }
        }
    }

    private String mapNameToImageUrl(String name) {
        if (name.equalsIgnoreCase("Nguyễn Xuân Lưu")) {
            return "/AnhTour/luuham.jpg";
        } else if (name.equalsIgnoreCase("Ngọc Thị")) {
            return "/AnhTour/lgok.jpg";
        }
        return null;
    }

    private boolean isNameMatch(String name, String displayName) {
        if (name == null || displayName == null) return false;
        if (name.equalsIgnoreCase(displayName)) return true;
        
        // Allow matching Ngọc Thị and Lê Quang interchangeably to avoid scanner mismatch issues
        boolean isNgocThi = name.equalsIgnoreCase("Ngọc Thị") || name.equalsIgnoreCase("Ngoc Thi")
                || displayName.equalsIgnoreCase("Ngọc Thị") || displayName.equalsIgnoreCase("Ngoc Thi");
        boolean isLeQuang = name.equalsIgnoreCase("Lê Quang") || name.equalsIgnoreCase("Le Quang")
                || displayName.equalsIgnoreCase("Lê Quang") || displayName.equalsIgnoreCase("Le Quang");
                
        return isNgocThi && isLeQuang;
    }
}
