package com.kawai.repositories;

import com.kawai.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tour reviews: có tourBooking hoặc ratingTour khác null
    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'Approved' AND (r.tourBooking IS NOT NULL OR r.ratingTour IS NOT NULL) ORDER BY r.createdAt DESC")
    List<Review> findApprovedTourReviews();

    // Room reviews: có roomBookingDetail hoặc ratingRoomDining khác null
    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'Approved' AND (r.roomBookingDetail IS NOT NULL OR r.ratingRoomDining IS NOT NULL) ORDER BY r.createdAt DESC")
    List<Review> findApprovedRoomReviews();

    // Other/General reviews: không có tour booking, không có ratingTour, không có
    // room booking, không có ratingRoomDining
    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'Approved' AND r.tourBooking IS NULL AND r.ratingTour IS NULL AND r.roomBookingDetail IS NULL AND r.ratingRoomDining IS NULL ORDER BY r.createdAt DESC")
    List<Review> findApprovedGeneralReviews();

    // All approved reviews
    List<Review> findByModerationStatusOrderByCreatedAtDesc(String status);

    boolean existsByTourBookingId(Long tourBookingId);

    boolean existsByRoomBookingDetailId(Long roomBookingDetailId);

    // Tour reviews by specific guide (via tourBooking -> schedule ->
    // TourStaffAssignment -> employee)
    @Query("SELECT r FROM Review r JOIN r.tourBooking tb JOIN tb.schedule s JOIN TourStaffAssignment tsa ON tsa.schedule = s WHERE r.moderationStatus = 'Approved' AND tsa.employee.id = :employeeId ORDER BY r.createdAt DESC")
    List<Review> findApprovedTourReviewsByGuideId(
            @org.springframework.data.repository.query.Param("employeeId") Long employeeId);
}