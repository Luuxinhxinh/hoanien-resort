package com.kawai.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.Booking;
import com.kawai.models.BookingService;
import com.kawai.models.HotelService;
import com.kawai.repositories.BookingRepository;
import com.kawai.repositories.BookingServiceRepository;
import com.kawai.repositories.HotelServiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/add-ons")
public class AddOnServiceApiController {

    private final HotelServiceRepository hotelServiceRepository;
    private final BookingServiceRepository bookingServiceRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    public AddOnServiceApiController(HotelServiceRepository hotelServiceRepository,
            BookingServiceRepository bookingServiceRepository,
            BookingRepository bookingRepository,
            ObjectMapper objectMapper) {
        this.hotelServiceRepository = hotelServiceRepository;
        this.bookingServiceRepository = bookingServiceRepository;
        this.bookingRepository = bookingRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<?> listCatalog(@RequestParam(defaultValue = "true") boolean availableOnly) {
        return ResponseEntity.ok(availableOnly
                ? hotelServiceRepository.findByIsAvailable(true)
                : hotelServiceRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> upsertCatalog(@RequestBody Map<String, Object> payload) {
        String name = stringValue(payload.get("serviceName"));
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "serviceName is required"));
        }
        BigDecimal basePrice = decimalValue(payload.get("basePrice"));
        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "basePrice must be non-negative"));
        }

        HotelService service = hotelServiceRepository.findByServiceNameIgnoreCase(name).orElseGet(HotelService::new);
        service.setServiceName(name);
        service.setBasePrice(basePrice);
        service.setSourceDepartment(stringValue(payload.getOrDefault("sourceDepartment", "SPA")));
        service.setDescription(stringValue(payload.get("description")));
        service.setIsAvailable(booleanValue(payload.getOrDefault("isAvailable", true)));
        return ResponseEntity.ok(hotelServiceRepository.save(service));
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAddOn(@RequestBody Map<String, Object> payload) {
        Long bookingId = longValue(payload.get("bookingId"));
        Long serviceId = longValue(payload.get("serviceId"));
        if (bookingId == null || serviceId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "bookingId and serviceId are required"));
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        HotelService service = hotelServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Add-on service not found"));
        if (!Boolean.TRUE.equals(service.getIsAvailable())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Add-on service is not available"));
        }

        BookingService bookingService = new BookingService();
        bookingService.setBooking(booking);
        bookingService.setService(service);
        bookingService.setQuantity(Math.max(1, intValue(payload.getOrDefault("quantity", 1))));
        bookingService.setUnitPrice(service.getBasePrice());
        bookingService.setExecutionDate(parseExecutionDate(payload.get("executionDate")));
        bookingService.setStatus("PENDING");
        bookingService.setSpecificRequestsJson(toJson(payload.get("specificRequests")));
        return ResponseEntity.ok(bookingServiceRepository.save(bookingService));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> listBookingAddOns(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingServiceRepository.findByBookingId(bookingId));
    }

    private LocalDateTime parseExecutionDate(Object value) {
        if (value == null || stringValue(value).isBlank()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(stringValue(value));
    }

    private String toJson(Object value) {
        if (value == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || stringValue(value).isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(stringValue(value));
    }

    private Boolean booleanValue(Object value) {
        return value instanceof Boolean b ? b : Boolean.parseBoolean(stringValue(value));
    }

    private Integer intValue(Object value) {
        return value instanceof Number n ? n.intValue() : Integer.parseInt(stringValue(value));
    }

    private Long longValue(Object value) {
        if (value == null || stringValue(value).isBlank()) {
            return null;
        }
        return value instanceof Number n ? n.longValue() : Long.parseLong(stringValue(value));
    }
}
