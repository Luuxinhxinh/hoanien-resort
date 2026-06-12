package com.kawai.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Tour_Attendees")
@Data
public class TourAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendee_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_booking_id", nullable = false)
    private TourBooking tourBooking;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "dependent_id")
    private Dependent dependent;

    @Column(name = "attendance_status", nullable = false)
    private String attendanceStatus = "Not_Show";

    @Column(name = "face_matched_at")
    private LocalDateTime faceMatchedAt;

    // Phá»¥c vá»¥ Ä‘iá»ƒm danh AI Face Scan
    @Column(name = "face_vector_data", columnDefinition = "TEXT")
    private String faceVectorData;

    public String getStatus() {
        return this.attendanceStatus;
    }

    public void setStatus(String status) {
        this.attendanceStatus = status;
    }
}
