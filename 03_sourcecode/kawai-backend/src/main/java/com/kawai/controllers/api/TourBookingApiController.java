package com.kawai.controllers.api;

import com.kawai.dto.TourBookingRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.TourBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tour-bookings")
public class TourBookingApiController {

    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @PostMapping
    public ResponseEntity<?> createTourBooking(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Long tourId = Long.valueOf(payload.get("tourId").toString());
            LocalDate departureDate = LocalDate.parse(payload.get("departureDate").toString(), DateTimeFormatter.ISO_LOCAL_DATE);
            String fullName = (String) payload.get("fullName");
            String email = (String) payload.get("email");
            String phone = (String) payload.get("phone");
            int participantCount = Integer.parseInt(payload.get("participantCount").toString());
            String paymentMethod = (String) payload.get("paymentMethod");
            String roomNumber = (String) payload.get("roomNumber");

            // 1. Find or create Customer
            Customer customer = null;
            if (principal != null) {
                customer = customerRepository.findByAccount_Username(principal.getName()).orElse(null);
            }
            if (customer == null) {
                // Try finding by email
                List<Customer> allCustomers = customerRepository.findAll();
                customer = allCustomers.stream()
                        .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                        .findFirst()
                        .orElse(null);
                
                if (customer == null) {
                    // Create transient Guest Customer
                    customer = new Customer();
                    customer.setFullName(fullName);
                    customer.setEmail(email);
                    customer.setPhone(phone);
                    customer.setGender("Nam"); // default
                    customer.setCccdPassportEncrypted("GUEST_" + System.currentTimeMillis());
                    customer.setLoyaltyPoints(0);
                    customer.setMembershipTier("Regular");
                    customer = customerRepository.save(customer);
                }
            }

            // 2. Find or create TourSchedule
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new IllegalArgumentException("Tour not found with ID: " + tourId));

            List<TourSchedule> schedules = tourScheduleRepository.findByTourIdAndDepartureDate(tourId, departureDate);
            TourSchedule schedule;
            if (schedules.isEmpty()) {
                schedule = new TourSchedule();
                schedule.setTour(tour);
                schedule.setDepartureDate(departureDate);
                schedule.setDepartureTime(LocalTime.of(8, 0));
                schedule.setBookedSeats(0);
                schedule.setScheduleStatus("Open");
                schedule = tourScheduleRepository.save(schedule);
            } else {
                schedule = schedules.get(0);
            }

            // 3. Prepare TourBookingRequest DTO
            TourBookingRequest request = new TourBookingRequest();
            request.setScheduleId(schedule.getId());
            request.setCustomerId(customer.getId());
            request.setParticipantCount(participantCount);
            request.setWalkInTour(false);

            if ("post-room".equalsIgnoreCase(paymentMethod)) {
                request.setPostToRoom(true);
                if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                    Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
                    if (roomOpt.isPresent() && roomOpt.get().getCurrentBookingDetailId() != null) {
                        request.setRoomBookingDetailId(roomOpt.get().getCurrentBookingDetailId());
                    }
                }
            } else {
                request.setPostToRoom(false);
            }

            // 4. Create Tour Booking
            Long bookingId = tourBookingService.createTourBooking(request);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "bookingId", bookingId,
                    "message", "Đặt tour thành công!"
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
