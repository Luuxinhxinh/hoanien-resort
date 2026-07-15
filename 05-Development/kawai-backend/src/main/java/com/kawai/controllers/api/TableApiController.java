package com.kawai.controllers.api;

import com.kawai.dto.TableReservationRequest;
import com.kawai.models.TableReservation;
import com.kawai.services.interfaces.TableReservationService;
import com.kawai.services.interfaces.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tables")
public class TableApiController {

    @Autowired
    private TableReservationService tableReservationService;

    @Autowired
    private TableService tableService;

    @GetMapping("/availability")
    public ResponseEntity<List<Long>> getAvailableTables(
            @RequestParam("date") String dateStr,
            @RequestParam("start") String startStr,
            @RequestParam("end") String endStr) {

        LocalDate date = LocalDate.parse(dateStr);
        LocalTime start = LocalTime.parse(startStr);
        LocalTime end = LocalTime.parse(endStr);

        List<Long> availableTableIds = tableReservationService.getAvailableTables(date, start, end);
        return ResponseEntity.ok(availableTableIds);
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(@RequestBody TableReservationRequest request, java.security.Principal principal) {
        try {
            TableReservation res = tableReservationService.createReservation(request, principal);
            return ResponseEntity.ok(Map.of("message", "Success", "reservationId", res.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/reservations/all")
    public ResponseEntity<List<Map<String, Object>>> getAllTablesWithReservations(
            @RequestParam(value = "date", required = false) String dateStr) {
        LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();
        List<Map<String, Object>> result = tableReservationService.getAllTablesWithReservations(date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<Map<String, Object>>> getTableReservations(
            @PathVariable("id") Long tableId,
            @RequestParam(value = "date", required = false) String dateStr) {

        LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();
        List<Map<String, Object>> result = tableReservationService.getTableReservations(tableId, date);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reservations/{id}/check-in")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('OP_FNB_TABLE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> checkInReservation(@PathVariable("id") Long reservationId) {
        try {
            tableReservationService.checkInReservation(reservationId);
            return ResponseEntity.ok(Map.of("message", "Check-in successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/reservations/{id}/hold")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('OP_FNB_TABLE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> holdReservation(@PathVariable("id") Long reservationId, @RequestBody Map<String, Integer> payload) {
        try {
            Integer holdMinutes = payload.getOrDefault("holdMinutes", 0);
            tableReservationService.holdReservation(reservationId, holdMinutes);
            return ResponseEntity.ok(Map.of("message", "Đã giữ bàn thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('OP_FNB_TABLE', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> updateTableStatus(@PathVariable("id") Long tableId, @RequestParam("status") String status) {
        try {
            tableService.toggleStatus(tableId, status);
            return ResponseEntity.ok(Map.of("message", "Table status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
