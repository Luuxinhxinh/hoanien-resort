package com.kawai.controllers.api;

import com.kawai.services.interfaces.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý API điểm danh tour bằng AI Face Scan (UC21).
 */
@RestController
@RequestMapping("/api/v1/tour-attendance")
public class TourAttendanceRestController {

    private final TourService tourService;

    public TourAttendanceRestController(TourService tourService) {
        this.tourService = tourService;
    }

    /**
     * API Xác thực khuôn mặt điểm danh tour.
     * 
     * @param attendeeId ID người tham gia tour
     * @param image      Ảnh khuôn mặt được upload
     * @return ResponseEntity mô tả trạng thái điểm danh
     */
    @PostMapping("/{attendeeId}/verify")
    public ResponseEntity<String> verifyAttendance(@PathVariable Long attendeeId, @RequestParam("image") MultipartFile image) {
        try {
            boolean isAttendanceVerified = tourService.verifyAttendance(attendeeId, image);
            if (isAttendanceVerified) {
                return ResponseEntity.ok("Attendance verified successfully");
            } else {
                return ResponseEntity.badRequest().body("Face match score below threshold");
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(503).body("AI Service unavailable: " + ex.getMessage());
        }
    }

    /**
     * API Điểm danh thủ công (Fallback khi hệ thống AI lỗi).
     * 
     * @param attendeeId ID người tham gia tour
     * @param status     Trạng thái điểm danh (VD: PRESENT, ABSENT)
     * @return ResponseEntity kết quả điểm danh
     */
    @PostMapping("/{attendeeId}/manual")
    public ResponseEntity<String> markManualAttendance(@PathVariable Long attendeeId, @RequestParam("status") String status) {
        try {
            tourService.markAttendanceManually(attendeeId, status);
            return ResponseEntity.ok("Attendance marked manually");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
