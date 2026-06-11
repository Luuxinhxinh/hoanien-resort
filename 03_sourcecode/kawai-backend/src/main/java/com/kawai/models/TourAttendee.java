package com.kawai.models;

import jakarta.persistence.*;
import lombok.Data;

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

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "is_checked_in", nullable = false)
    private Boolean isCheckedIn = false;

    // Phá»¥c vá»¥ Ä‘iá»ƒm danh AI Face Scan
    @Column(name = "face_vector_data", columnDefinition = "TEXT")
    private String faceVectorData;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;
}
