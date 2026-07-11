package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Tour_Schedules")
public class TourSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "booked_seats", nullable = false)
    private Integer bookedSeats = 0;

    @Column(name = "schedule_status", nullable = false)
    private String scheduleStatus = "Open";

    @Column(name = "is_insurance_processed", nullable = false)
    private Boolean isInsuranceProcessed = false;

    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    @Column(name = "actual_start_time")
    private java.time.LocalDateTime actualStartTime;

    // ── Getters ───────────────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public Tour getTour() {
        return tour;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public Integer getBookedSeats() {
        return bookedSeats;
    }

    public String getScheduleStatus() {
        return scheduleStatus;
    }

    public Boolean getIsInsuranceProcessed() {
        return isInsuranceProcessed;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public java.time.LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    // ── Setters ───────────────────────────────────────────────────────
    public void setId(Long id) {
        this.id = id;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public void setBookedSeats(Integer bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public void setScheduleStatus(String scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public void setIsInsuranceProcessed(Boolean isInsuranceProcessed) {
        this.isInsuranceProcessed = isInsuranceProcessed;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    public void setActualStartTime(java.time.LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    @Override
    public String toString() {
        return "TourSchedule{id=" + id + ", departureDate=" + departureDate + ", scheduleStatus='" + scheduleStatus
                + "', actualStartTime=" + actualStartTime + "}";
    }
}
