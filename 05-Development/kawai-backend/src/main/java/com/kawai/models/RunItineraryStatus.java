package com.kawai.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Run_Itinerary_Status")
public class RunItineraryStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_status_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @ManyToOne
    @JoinColumn(name = "detail_id", nullable = false)
    private TourItineraryDetail detail;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Column(name = "current_stage_status", nullable = false)
    private String currentStageStatus = "NOT_STARTED";

    @Column(name = "guide_notes", columnDefinition = "TEXT")
    private String guideNotes;

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

    public TourItineraryDetail getDetail() {
        return detail;
    }

    public void setDetail(TourItineraryDetail detail) {
        this.detail = detail;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getCurrentStageStatus() {
        return currentStageStatus;
    }

    public void setCurrentStageStatus(String currentStageStatus) {
        this.currentStageStatus = currentStageStatus;
    }

    public String getGuideNotes() {
        return guideNotes;
    }

    public void setGuideNotes(String guideNotes) {
        this.guideNotes = guideNotes;
    }
}
