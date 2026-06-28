package com.kawai.controllers.api;

import com.kawai.models.TourStaffAssignment;
import com.kawai.repositories.TourStaffAssignmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tours/gps")
public class TourGpsApiController {

    private final TourStaffAssignmentRepository assignmentRepo;
    private final Map<Long, TourGpsPoint> latestBySchedule = new ConcurrentHashMap<>();

    public TourGpsApiController(TourStaffAssignmentRepository assignmentRepo) {
        this.assignmentRepo = assignmentRepo;
    }

    @PostMapping("/{scheduleId}")
    public ResponseEntity<?> updateLocation(@PathVariable Long scheduleId, @RequestBody Map<String, Object> payload) {
        Double latitude = doubleValue(payload.get("latitude"));
        Double longitude = doubleValue(payload.get("longitude"));
        if (latitude == null || longitude == null || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return ResponseEntity.badRequest().body(Map.of("message", "latitude/longitude are invalid"));
        }

        // Persist to DB: update lead guide GPS position
        try {
            List<TourStaffAssignment> assignments = assignmentRepo.findByScheduleId(scheduleId);
            for (TourStaffAssignment a : assignments) {
                if (Boolean.TRUE.equals(a.getIsLeadGuide())) {
                    a.setCurrentGpsLat(BigDecimal.valueOf(latitude));
                    a.setCurrentGpsLng(BigDecimal.valueOf(longitude));
                    assignmentRepo.save(a);
                }
            }
        } catch (Exception e) {
            // DB may not have assignments yet; use in-memory as fallback
        }

        TourGpsPoint point = new TourGpsPoint(
                scheduleId, latitude, longitude,
                stringValue(payload.getOrDefault("vehicleCode", "TOUR-" + scheduleId)),
                stringValue(payload.getOrDefault("note", "")),
                LocalDateTime.now());
        latestBySchedule.put(scheduleId, point);
        return ResponseEntity.ok(point);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> latest(@PathVariable Long scheduleId) {
        TourGpsPoint point = latestBySchedule.get(scheduleId);
        if (point == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(point);
    }

    @GetMapping
    public ResponseEntity<?> latestAll() {
        return ResponseEntity.ok(latestBySchedule.values().stream()
                .sorted(Comparator.comparing(TourGpsPoint::updatedAt).reversed())
                .toList());
    }

    private Double doubleValue(Object value) {
        if (value == null || stringValue(value).isBlank()) return null;
        return value instanceof Number n ? n.doubleValue() : Double.parseDouble(stringValue(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public record TourGpsPoint(Long scheduleId, Double latitude, Double longitude, String vehicleCode, String note, LocalDateTime updatedAt) {}
}
