package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name="Reviews") @Data
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="review_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @ManyToOne @JoinColumn(name="room_booking_detail_id") private RoomBookingDetail roomBookingDetail;
    @ManyToOne @JoinColumn(name="tour_booking_id") private TourBooking tourBooking;
    @Column(name="rating_service", nullable=false) private Integer ratingService;
    @Column(name="review_text", columnDefinition="TEXT") private String reviewText;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="moderation_status", nullable=false) private String moderationStatus = "Pending";
    @ManyToOne @JoinColumn(name="moderated_by") private Employee moderatedBy;
    @Column(name="moderation_reason", columnDefinition="TEXT") private String moderationReason;
}
