package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
@Entity @Table(name="Tour_Schedules") @Data
public class TourSchedule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="schedule_id") private Long id;
    @ManyToOne @JoinColumn(name="tour_id", nullable=false) private Tour tour;
    @Column(name="departure_date", nullable=false) private LocalDate departureDate;
    @Column(name="departure_time", nullable=false) private LocalTime departureTime;
    @Column(name="booked_seats", nullable=false) private Integer bookedSeats = 0;
    @Column(name="schedule_status", nullable=false) private String scheduleStatus = "Open";
}
