package com.kawai.controllers.api;

import com.kawai.models.Employee;
import com.kawai.models.HotelOperation;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.HotelOperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tour-incidents")
public class TourIncidentApiController {

    @Autowired
    private HotelOperationRepository hotelOperationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_TOURGUIDE', 'OP_TOUR')")
    public ResponseEntity<?> reportIncident(@RequestBody Map<String, String> request) {
        try {
            String description = request.get("description");
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mô tả sự cố không được để trống."));
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Employee staff = employeeRepository.findByAccountUsername(username).orElse(null);

            HotelOperation incident = new HotelOperation();
            incident.setOperationalType("TOUR_INCIDENT");
            incident.setPriority("Urgent");
            incident.setStatus("Pending");
            incident.setNotes(description);
            incident.setStaff(staff);
            incident.setCreatedAt(LocalDateTime.now());

            hotelOperationRepository.save(incident);

            return ResponseEntity.ok(Map.of("success", true, "message", "Báo cáo sự cố đã được gửi thành công."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> getPendingIncidents() {
        try {
            List<HotelOperation> pendingOps = hotelOperationRepository.findByStatus("Pending");
            List<Map<String, Object>> incidents = pendingOps.stream()
                    .filter(op -> "TOUR_INCIDENT".equals(op.getOperationalType()))
                    .map(op -> Map.<String, Object>of(
                            "id", op.getId(),
                            "description", op.getNotes(),
                            "staffName", op.getStaff() != null ? op.getStaff().getFullName() : "N/A",
                            "createdAt", op.getCreatedAt().toString()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", incidents));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> resolveIncident(@PathVariable Long id) {
        try {
            HotelOperation incident = hotelOperationRepository.findById(id).orElse(null);
            if (incident == null || !"TOUR_INCIDENT".equals(incident.getOperationalType())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy sự cố."));
            }

            incident.setStatus("Completed");
            incident.setCompletedAt(LocalDateTime.now());
            
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Employee manager = employeeRepository.findByAccountUsername(username).orElse(null);
            incident.setSupervisor(manager);

            hotelOperationRepository.save(incident);

            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xử lý sự cố."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
