package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Checkpoint_Attendance")
public class CheckpointAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkpoint_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @ManyToOne
    @JoinColumn(name = "attendee_id", nullable = false)
    private TourAttendee attendee;

    @ManyToOne
    @JoinColumn(name = "detail_id", nullable = false)
    private TourItineraryDetail detail;

    @Column(name = "scan_status", nullable = false)
    private String scanStatus;

    @Column(name = "scanned_at", nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime scannedAt;

    @ManyToOne
    @JoinColumn(name = "scanned_by_staff_id", nullable = false)
    private Employee scannedByStaff;

    // Getters / Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TourSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TourSchedule schedule) {
        this.schedule = schedule;
    }

    public TourAttendee getAttendee() {
        return attendee;
    }

    public void setAttendee(TourAttendee attendee) {
        this.attendee = attendee;
    }

    public TourItineraryDetail getDetail() {
        return detail;
    }

    public void setDetail(TourItineraryDetail detail) {
        this.detail = detail;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public Employee getScannedByStaff() {
        return scannedByStaff;
    }

    public void setScannedByStaff(Employee scannedByStaff) {
        this.scannedByStaff = scannedByStaff;
    }
}
