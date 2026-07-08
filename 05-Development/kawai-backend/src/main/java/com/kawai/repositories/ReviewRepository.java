package com.kawai.repositories;

import com.kawai.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tour reviews: có tourBooking và ratingTour != null
    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'Approved' AND r.tourBooking IS NOT NULL AND r.ratingTour IS NOT NULL ORDER BY r.createdAt DESC")
    List<Review> findApprovedTourReviews();

    // Room reviews: có roomBookingDetail hoặc ratingRoomDining != null
    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'Approved' AND (r.roomBookingDetail IS NOT NULL OR r.ratingRoomDining IS NOT NULL) ORDER BY r.createdAt DESC")
    List<Review> findApprovedRoomReviews();

    // Other/General reviews: không có cả tour booking lẫn room booking
    @Query("SELECT r FROM Review r WHERE r.moderationStatus = 'Approved' AND r.tourBooking IS NULL AND r.roomBookingDetail IS NULL ORDER BY r.createdAt DESC")
    List<Review> findApprovedGeneralReviews();

    // All approved reviews
    List<Review> findByModerationStatusOrderByCreatedAtDesc(String status);
}