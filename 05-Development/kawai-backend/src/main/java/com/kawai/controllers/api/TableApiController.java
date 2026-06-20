package com.kawai.controllers.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalTime;

import com.kawai.models.TableReservation;
import com.kawai.models.RestaurantTable;
import com.kawai.models.FoodOrder;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.dto.TableReservationRequest;
import com.kawai.dto.OpenTableRequest;

@RestController
@RequestMapping("/api/v1/tables")
public class TableApiController {

    @Autowired
    private TableReservationRepository tableReservationRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @GetMapping("/availability")
    public ResponseEntity<List<Long>> getAvailableTables(
            @RequestParam("date") String dateStr,
            @RequestParam("start") String startStr,
            @RequestParam("end") String endStr) {
        
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime start = LocalTime.parse(startStr);
        LocalTime end = LocalTime.parse(endStr);
        
        List<RestaurantTable> allTables = restaurantTableRepository.findAll();
        List<Long> availableTableIds = new ArrayList<>();
        
        for (RestaurantTable table : allTables) {
            List<TableReservation> reservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), date);
            boolean isAvailable = true;
            for (TableReservation res : reservations) {
                if ("Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus())) {
                    LocalTime rStart = res.getReserveTime();
                    LocalTime rEnd = res.getEndTime() != null ? res.getEndTime() : rStart.plusHours(2);
                    
                    if (rStart.isBefore(end) && rEnd.isAfter(start)) {
                        isAvailable = false;
                        break;
                    }
                }
            }
            
            // Check physical status for immediate bookings (within 2 hours of now)
            if (isAvailable && date.equals(LocalDate.now())) {
                if ("Occupied".equalsIgnoreCase(table.getTableStatus()) || "Cleaning".equalsIgnoreCase(table.getTableStatus())) {
                    // Check if the start time is within the next 2 hours.
                    // Handle wrap-around by using LocalDateTime.
                    java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(date, start);
                    if (startDateTime.isBefore(java.time.LocalDateTime.now().plusHours(2))) {
                        isAvailable = false;
                    }
                }
            }

            if (isAvailable) {
                availableTableIds.add(table.getId());
            }
        }
        
        return ResponseEntity.ok(availableTableIds);
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(@RequestBody TableReservationRequest request) {
        try {
            RestaurantTable table = restaurantTableRepository.findById(request.getTableId()).orElse(null);
            if (table == null) return ResponseEntity.badRequest().body("Table not found");
            
            // Check capacity
            if (request.getPartySize() > table.getCapacity()) {
                return ResponseEntity.badRequest().body("Party size exceeds table capacity");
            }

            // Check for conflicts
            List<TableReservation> existingReservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(table.getId(), request.getReserveDate());
            LocalTime newStart = request.getStartTime();
            LocalTime newEnd = request.getEndTime() != null ? request.getEndTime() : newStart.plusHours(2);

            for (TableReservation res : existingReservations) {
                if ("Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus())) {
                    LocalTime existingStart = res.getReserveTime();
                    LocalTime existingEnd = res.getEndTime() != null ? res.getEndTime() : existingStart.plusHours(2);
                    
                    if (existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart)) {
                        return ResponseEntity.badRequest().body("Table is already reserved for the requested time");
                    }
                }
            }

            TableReservation res = new TableReservation();
            res.setTable(table);
            res.setReserveDate(request.getReserveDate());
            res.setReserveTime(request.getStartTime());
            res.setEndTime(request.getEndTime());
            res.setPartySize(request.getPartySize());
            res.setSpecialRequests(request.getSpecialRequests());
            res.setStatus("Pending");
            
            // Mock customer
            customerRepository.findById(1L).ifPresent(res::setCustomer);
            
            tableReservationRepository.save(res);
            
            return ResponseEntity.ok(Map.of("message", "Success", "reservationId", res.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<Map<String, Object>>> getTableReservations(
            @org.springframework.web.bind.annotation.PathVariable("id") Long tableId,
            @RequestParam(value = "date", required = false) String dateStr) {
        
        LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();
        List<TableReservation> reservations = tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(tableId, date);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (TableReservation res : reservations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", res.getId());
            map.put("reserveDate", res.getReserveDate().toString());
            map.put("reserveTime", res.getReserveTime().toString());
            map.put("endTime", res.getEndTime() != null ? res.getEndTime().toString() : res.getReserveTime().plusHours(2).toString());
            map.put("partySize", res.getPartySize());
            map.put("status", res.getStatus());
            map.put("specialRequests", res.getSpecialRequests());
            if (res.getCustomer() != null) {
                map.put("customerName", res.getCustomer().getFullName());
            } else {
                map.put("customerName", "Khách hàng");
            }
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }


}
