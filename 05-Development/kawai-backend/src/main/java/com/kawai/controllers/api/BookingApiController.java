package com.kawai.controllers.api;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.services.interfaces.BookingService;
import com.kawai.repositories.CustomerRepository;
import com.kawai.models.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO request, Principal principal) {
        try {
            // Resolve logged-in customer if possible
            if (principal != null) {
                String username = principal.getName();
                Optional<Customer> customerOpt = customerRepository.findByAccount_Username(username);
                if (customerOpt.isPresent()) {
                    request.setCustomerId(customerOpt.get().getId());
                }
            }

            // Mock a customer ID if still null, for demo/testing purposes
            // if (request.getCustomerId() == null) {
            // request.setCustomerId(1L); // Default customer ID for demo
            // }

            BookingResponseDTO response = bookingService.createBooking(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "bookingId", response.getBookingId(),
                    "depositAmount", response.getDepositAmount(),
                    "cancellationDeadline", response.getCancellationDeadline()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }
}
