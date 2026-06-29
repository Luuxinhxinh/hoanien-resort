package com.kawai.controllers.api;

import com.kawai.dto.TourBookingRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.TourBookingService;
import com.kawai.services.interfaces.VnPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private VnPayService vnPayService;

    @PostMapping
    public ResponseEntity<?> createTourBooking(@RequestBody Map<String, Object> payload, Principal principal,
            HttpServletRequest httpRequest) {
        try {
            Long tourId = Long.valueOf(payload.get("tourId").toString());
            LocalDate departureDate = LocalDate.parse(payload.get("departureDate").toString(),
                    DateTimeFormatter.ISO_LOCAL_DATE);
            String fullName = (String) payload.get("fullName");
            String email = (String) payload.get("email");
            String phone = (String) payload.get("phone");
            int participantCount = Integer.parseInt(payload.get("participantCount").toString());
            String paymentMethod = (String) payload.get("paymentMethod");
            String roomNumber = (String) payload.get("roomNumber");

            // 1. Find or create Customer
            Customer customer = null;
            if (principal != null) {
                String identifier = principal.getName();
                if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                    identifier = oauthToken.getPrincipal().getAttribute("email");
                }
                if (identifier != null) {
                    customer = customerRepository.findByAccount_Username(identifier).orElse(null);
                }
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
                    customer.setMembershipTier(
                            membershipTierRepository.findByTierNameIgnoreCase("Regular").orElse(null));
                    customer = customerRepository.save(customer);
                }
            }

            // 2. Find or create TourSchedule
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new IllegalArgumentException("Tour not found with ID: " + tourId));

            List<TourSchedule> schedules = tourScheduleRepository.findByTourIdAndDepartureDate(tourId, departureDate);
            TourSchedule schedule = schedules.isEmpty() ? null : schedules.get(0);

            // Determine guide for this booking
            String guideForNewTour = getGuideNameForTour(tour.getTourName());
            boolean isSpecialCustomer = "ngocnguyenthuy999@gmail.com".equalsIgnoreCase(email);
            if (isSpecialCustomer) {
                guideForNewTour = "NguynNgoc";
            }

            // Verify guide limit: 1 guide per day
            List<TourSchedule> existingSchedulesOnDay = tourScheduleRepository.findByDepartureDate(departureDate);
            for (TourSchedule es : existingSchedulesOnDay) {
                // If it is a different schedule
                if (schedule == null || !es.getId().equals(schedule.getId())) {
                    int bookingsCount = tourBookingRepository.countByScheduleAndBookingStatus(es, "Confirmed");
                    if (bookingsCount > 0 && es.getTour() != null) {
                        String existingGuide = getGuideNameForTour(es.getTour().getTourName());
                        if (scheduleHasSpecialCustomer(es)) {
                            existingGuide = "NguynNgoc";
                        }
                        if (existingGuide.equalsIgnoreCase(guideForNewTour)) {
                            throw new IllegalArgumentException("Hướng dẫn viên phụ trách (" + guideForNewTour + ") đã kẹt lịch trình tour khác trong ngày này. Vui lòng chọn ngày khác.");
                        }
                    }
                }
            }

            if (schedule == null) {
                schedule = new TourSchedule();
                schedule.setTour(tour);
                schedule.setDepartureDate(departureDate);
                schedule.setDepartureTime(LocalTime.of(8, 0));
                schedule.setBookedSeats(0);
                schedule.setScheduleStatus("Open");
                schedule = tourScheduleRepository.save(schedule);
            }

            // 3. Prepare TourBookingRequest DTO
            TourBookingRequest request = new TourBookingRequest();
            request.setScheduleId(schedule.getId());
            request.setCustomerId(customer.getId());
            request.setParticipantCount(participantCount);
            request.setParticipantCount(participantCount);

            RoomBookingDetail detail = null;

            if (roomNumber == null || roomNumber.trim().isEmpty()) {
                // Tự động tìm booking detail đang hoạt động của khách hàng này để liên kết
                List<RoomBookingDetail> customerDetails = roomBookingDetailRepository.findByCustomer(customer);
                if (customerDetails.isEmpty()) {
                    throw new IllegalArgumentException("Vui lòng thực hiện đặt phòng trước khi đặt tour.");
                }
                
                // Tìm xem có cái nào đang hoạt động và bao gồm ngày tour không
                for (RoomBookingDetail rbd : customerDetails) {
                    if ("Confirmed".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus())
                            || "Checked_In".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus())) {
                        LocalDate checkIn = rbd.getRoomBooking().getCheckInDate();
                        LocalDate checkOut = rbd.getRoomBooking().getCheckOutDate();
                        if (!departureDate.isBefore(checkIn) && !departureDate.isAfter(checkOut)) {
                            detail = rbd;
                            break;
                        }
                    }
                }
                
                if (detail == null) {
                    throw new IllegalArgumentException("Khách đặt phòng đã hết thời hạn, cần đăng ký phòng mới.");
                }
            } else {
                if (roomNumber.toUpperCase().startsWith("VIRTUAL_")) {
                    try {
                        Long detailId = Long.parseLong(roomNumber.substring(8));
                        detail = roomBookingDetailRepository.findById(detailId).orElse(null);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Mã đặt phòng không hợp lệ.");
                    }
                } else {
                    Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
                    if (roomOpt.isEmpty()) {
                        throw new IllegalArgumentException("Phòng không hợp lệ.");
                    }
                    Room room = roomOpt.get();

                    // 1. Kiểm tra nếu khách đã check-in (phòng có currentBookingDetailId)
                    if (room.getCurrentBookingDetailId() != null) {
                        detail = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId()).orElse(null);
                    }

                    // 2. Nếu chưa check-in, tìm Booking Detail đã được xác nhận (Confirmed) của khách cho phòng này
                    if (detail == null) {
                        List<RoomBookingDetail> customerDetails = roomBookingDetailRepository.findByCustomer(customer);
                        for (RoomBookingDetail rbd : customerDetails) {
                            if (rbd.getRoom() != null && rbd.getRoom().getRoomNumber().equals(roomNumber)
                                    && ("Confirmed".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus())
                                        || "Checked_In".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus()))) {
                                detail = rbd;
                                break;
                            }
                        }
                    }
                }
                
                if (detail == null) {
                    // Check if they ever booked any room
                    List<RoomBookingDetail> customerDetails = roomBookingDetailRepository.findByCustomer(customer);
                    if (customerDetails.isEmpty()) {
                        throw new IllegalArgumentException("Vui lòng thực hiện đặt phòng trước khi đặt tour.");
                    } else {
                        throw new IllegalArgumentException("Khách đặt phòng đã hết thời hạn, cần đăng ký phòng mới.");
                    }
                }

                // Check if the tour departure date is outside the room booking duration
                LocalDate checkIn = detail.getRoomBooking().getCheckInDate();
                LocalDate checkOut = detail.getRoomBooking().getCheckOutDate();
                if (departureDate.isBefore(checkIn) || departureDate.isAfter(checkOut)) {
                    throw new IllegalArgumentException("Khách đặt phòng đã hết thời hạn, cần đăng ký phòng mới.");
                }
            }

            request.setRoomBookingDetailId(detail.getId());
            request.setRoomBookingId(detail.getRoomBooking().getId());

            List<String> childAges = (List<String>) payload.get("childAges");
            request.setChildAges(childAges);

            // Mã giảm giá (tùy chọn)
            String promoCode = payload.get("promoCode") != null ? payload.get("promoCode").toString() : null;
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                request.setPromoCode(promoCode.trim());
            }


            if ("post-room".equalsIgnoreCase(paymentMethod)) {
                request.setPostToRoom(true);
            } else {
                request.setPostToRoom(false);
            }
            request.setPaymentMethod(paymentMethod);
            request.setVnpPaymentType(payload.get("vnpPaymentType") != null ? payload.get("vnpPaymentType").toString() : null);
            request.setNotes(payload.get("notes") != null ? payload.get("notes").toString() : null);

            // 4. Create Tour Booking
            Long bookingId = tourBookingService.createTourBooking(request);

            // Build detailed success payload with customer info
            Map<String, Object> responsePayload = new java.util.HashMap<>();
            responsePayload.put("status", "success");
            responsePayload.put("bookingId", bookingId);
            responsePayload.put("message", "Đặt tour thành công!");
            responsePayload.put("tourName", tour.getTourName());

            // Calculate totalPrice considering children discount
            java.math.BigDecimal totalPrice = java.math.BigDecimal.ZERO;
            java.math.BigDecimal basePrice = tour.getBasePrice();
            int childCount = childAges != null ? childAges.size() : 0;
            int adultCount = participantCount - childCount;
            if (adultCount < 0)
                adultCount = 0;

            totalPrice = totalPrice.add(basePrice.multiply(java.math.BigDecimal.valueOf(adultCount)));
            if (childAges != null) {
                for (String age : childAges) {
                    if ("Dưới 2 tuổi".equalsIgnoreCase(age)) {
                        // free
                    } else if ("2 - 11 tuổi".equalsIgnoreCase(age)) {
                        totalPrice = totalPrice.add(basePrice.multiply(new java.math.BigDecimal("0.5")));
                    } else {
                        totalPrice = totalPrice.add(basePrice.multiply(new java.math.BigDecimal("0.5")));
                    }
                }
            }

            responsePayload.put("totalPrice", totalPrice);
            responsePayload.put("paymentMethod", paymentMethod);
            java.math.BigDecimal depositAmount = totalPrice.multiply(new java.math.BigDecimal("0.3"));
            responsePayload.put("depositAmount", depositAmount);
            responsePayload.put("roomNumber", roomNumber);
            if (customer != null) {
                responsePayload.put("customerName", customer.getFullName());
                responsePayload.put("customerEmail", customer.getEmail());
                responsePayload.put("customerPhone", customer.getPhone());
            }

            // If VNPay payment, generate payment URL
            if ("vnpay".equalsIgnoreCase(paymentMethod)) {
                // Determine the payment type sent from frontend: "deposit" or "full"
                String vnpPaymentType = (String) payload.getOrDefault("vnpPaymentType", "deposit");
                java.math.BigDecimal payAmount = "full".equalsIgnoreCase(vnpPaymentType) ? totalPrice : depositAmount;
                try {
                    String clientIp = httpRequest.getHeader("X-Forwarded-For");
                    if (clientIp == null || clientIp.isBlank())
                        clientIp = httpRequest.getRemoteAddr();
                    String vnpayUrl = vnPayService.createPaymentUrlForTourBooking(bookingId, payAmount, vnpPaymentType,
                            clientIp);
                    responsePayload.put("vnpayUrl", vnpayUrl);
                } catch (Exception vnpEx) {
                    responsePayload.put("vnpayError", vnpEx.getMessage());
                }
            }

            return ResponseEntity.ok(responsePayload);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTourBookingDetail(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        Optional<TourBooking> optTb = tourBookingRepository.findById(id);
        if (optTb.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn đặt tour"));
        }
        TourBooking tb = optTb.get();
        TourSchedule schedule = tb.getSchedule();
        Tour tour = schedule != null ? schedule.getTour() : null;

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", tb.getId());
        data.put("bookingStatus", tb.getBookingStatus());
        data.put("participantCount", tb.getParticipantCount());
        data.put("tourCharge", tb.getTourCharge());
        data.put("paymentType", "UNKNOWN");

        if (schedule != null) {
            data.put("departureDate", schedule.getDepartureDate().toString());
            data.put("departureTime", schedule.getDepartureTime().toString());
            if (tour != null) {
                data.put("tourName", tour.getTourName());
                data.put("duration", tour.getDuration());
                data.put("description", tour.getDescription());
                // Get itineraries if needed, but for now we just return the basics
            }
        }
        return ResponseEntity.ok(data);
    }

    private String getGuideNameForTour(String tourName) {
        if (tourName == null) return "NguynNgoc";
        String tn = tourName.toLowerCase();
        if (tn.contains("tinh túy đồng nội") || tn.contains("tinh túy đồng nội") || tn.contains("đồng nội") || tn.contains("dongnoi")) {
            return "Ngọc Lan";
        } else if (tn.contains("tĩnh lặng liên hoa") || tn.contains("tĩnh lặng liên hoa") || tn.contains("tinhlang")) {
            return "Hoàng Nam";
        } else if (tn.contains("di sản") || tn.contains("disan")) {
            return "Ngọc Lan";
        }
        return "NguynNgoc";
    }

    private boolean scheduleHasSpecialCustomer(TourSchedule sched) {
        if (sched == null || sched.getId() == null) return false;
        try {
            java.util.List<TourBooking> bookings = tourBookingRepository.findBySchedule(sched);
            if (bookings != null) {
                for (TourBooking b : bookings) {
                    if (b.getCustomer() != null && "ngocnguyenthuy999@gmail.com".equalsIgnoreCase(b.getCustomer().getEmail())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
