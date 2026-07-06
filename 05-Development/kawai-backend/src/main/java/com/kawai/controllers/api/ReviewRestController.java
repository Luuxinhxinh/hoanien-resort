package com.kawai.controllers.api;

import com.kawai.models.Review;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.services.interfaces.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho tính năng Đánh giá dịch vụ (UC22, UC23).
 */
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewRestController {

    private final ReviewService reviewService;
    private final EmployeeRepository employeeRepository;

    public ReviewRestController(ReviewService reviewService, EmployeeRepository employeeRepository) {
        this.reviewService = reviewService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * API Khách hàng gửi đánh giá Tour.
     *
     * @param customerId    ID khách hàng
     * @param tourBookingId ID đơn đặt tour
     * @param rating        Số sao đánh giá (1-5)
     * @param reviewText    Nội dung đánh giá
     * @return 200 OK nếu thành công, 400 Bad Request nếu lỗi logic
     */
    @PostMapping("/tour")
    public ResponseEntity<?> submitTourReview(
            @RequestParam Long customerId,
            @RequestParam Long tourBookingId,
            @RequestParam Integer rating,
            @RequestParam String reviewText) {
        try {
            Review savedReview = reviewService.submitTourReview(customerId, tourBookingId, rating, reviewText);
            return ResponseEntity.ok(savedReview);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * API Admin kiểm duyệt đánh giá (UC23).
     * Bắt buộc phải có lý do kiểm duyệt (BR-TR-04).
     *
     * @param reviewId  ID của đánh giá cần kiểm duyệt (Path Variable)
     * @param adminId   ID của Admin đang thao tác (Request Param)
     * @param newStatus Trạng thái kiểm duyệt mới (Request Param)
     * @param reason    Lý do kiểm duyệt (Request Param)
     * @return 200 OK trả về Review đã kiểm duyệt, 400 Bad Request nếu lỗi logic/validation
     */
    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<?> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long adminId,
            @RequestParam String newStatus,
            @RequestParam String reason,
            Authentication authentication) {
        try {
            Long moderatorId = resolveModeratorId(adminId, authentication);
            Review moderatedReview = reviewService.moderateReview(reviewId, moderatorId, newStatus, reason);
            return ResponseEntity.ok(moderatedReview);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private Long resolveModeratorId(Long adminId, Authentication authentication) {
        if (adminId != null) {
            return adminId;
        }
        if (authentication != null && authentication.getName() != null) {
            return employeeRepository.findByAccountUsername(authentication.getName())
                    .map(com.kawai.models.Employee::getId)
                    .orElseGet(this::firstEmployeeId);
        }
        return firstEmployeeId();
    }

    private Long firstEmployeeId() {
        return employeeRepository.findAll().stream()
                .findFirst()
                .map(com.kawai.models.Employee::getId)
                .orElseThrow(() -> new IllegalArgumentException("No employee account available for moderation"));
    }
}
