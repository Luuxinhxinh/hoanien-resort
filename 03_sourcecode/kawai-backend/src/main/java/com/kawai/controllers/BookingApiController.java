package com.kawai.controllers;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.services.interfaces.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO request) {
        try {
            // Mock a customer ID if not provided, for demo purposes
            if (request.getCustomerId() == null) {
                request.setCustomerId(1L); // Default customer ID for demo
            }
            
            BookingResponseDTO response = bookingService.createBooking(request);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "bookingId", response.getBookingId(),
                "depositAmount", response.getDepositAmount(),
                "cancellationDeadline", response.getCancellationDeadline()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                "status", "error",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }
}
