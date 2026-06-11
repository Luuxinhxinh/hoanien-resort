package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name="Tour_Schedules") @Data
public class TourSchedule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="schedule_id") private Long id;
    @ManyToOne @JoinColumn(name="tour_id", nullable=false) private Tour tour;
    @Column(name="departure_date", nullable=false) private LocalDate departureDate;
    @Column(name="available_slots", nullable=false) private Integer availableSlots;
    @Column(name="schedule_status", nullable=false) private String scheduleStatus = "Open";
}
