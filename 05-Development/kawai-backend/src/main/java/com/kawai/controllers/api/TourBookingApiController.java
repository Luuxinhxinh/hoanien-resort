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

    @Autowired
    private TourAttendeeRepository tourAttendeeRepository;

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

            // Parse roomSelections: [{roomId, adults, children}] — per-room participant counts
            // Also support legacy roomNumbers[] (array of strings) and roomNumber (single string)
            List<java.util.Map<String, Object>> roomSelections = null;
            Object roomSelectionsRaw = payload.get("roomSelections");
            if (roomSelectionsRaw instanceof List) {
                roomSelections = (List<java.util.Map<String, Object>>) roomSelectionsRaw;
            }

            // Legacy fallbacks
            List<String> legacyRoomNumbers = null;
            if (roomSelections == null) {
                Object roomNumbersRaw = payload.get("roomNumbers");
                if (roomNumbersRaw instanceof List) {
                    legacyRoomNumbers = (List<String>) roomNumbersRaw;
                } else {
                    String singleRoom = payload.get("roomNumber") != null ? payload.get("roomNumber").toString() : null;
                    if (singleRoom != null) legacyRoomNumbers = java.util.Collections.singletonList(singleRoom);
                }
            }

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
                }
            }

            String cccdPassport = (String) payload.get("cccdPassport");
            if (cccdPassport != null && !cccdPassport.trim().isEmpty() && customer != null) {
                customer.setCccdPassportEncrypted(cccdPassport.trim());
                customer = customerRepository.save(customer);
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



            if (schedule == null) {
                schedule = new TourSchedule();
                schedule.setTour(tour);
                schedule.setDepartureDate(departureDate);
                schedule.setDepartureTime(LocalTime.of(8, 0));
                schedule.setBookedSeats(0);
                schedule.setScheduleStatus("Open");
                schedule = tourScheduleRepository.save(schedule);
            }

            // 3. Prepare shared TourBookingRequest template (room detail set per-iteration below)
            TourBookingRequest request = new TourBookingRequest();
            request.setScheduleId(schedule.getId());
            request.setCustomerId(customer.getId());
            request.setParticipantCount(participantCount);

            List<String> childAges = (List<String>) payload.get("childAges");
            request.setChildAges(childAges);

            List<Map<String, Object>> companionsRaw = (List<Map<String, Object>>) payload.get("companions");
            List<TourBookingRequest.CompanionRequest> companions = new java.util.ArrayList<>();
            if (companionsRaw != null) {
                for (Map<String, Object> compMap : companionsRaw) {
                    TourBookingRequest.CompanionRequest comp = new TourBookingRequest.CompanionRequest();
                    comp.setName((String) compMap.get("name"));
                    comp.setAge(compMap.get("age") != null ? Integer.parseInt(compMap.get("age").toString()) : null);
                    comp.setPhone((String) compMap.get("phone"));
                    comp.setIdCard((String) compMap.get("idCard"));
                    companions.add(comp);
                }
            }
            request.setCompanions(companions);

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

            boolean acceptInsurance = payload.get("acceptInsurance") != null && Boolean.parseBoolean(payload.get("acceptInsurance").toString());
            request.setAcceptInsurance(acceptInsurance);

            // 4. Create one TourBooking per selected room
            List<Long> createdBookingIds = new java.util.ArrayList<>();
            List<String> resolvedRooms = new java.util.ArrayList<>();

            if (roomSelections != null && !roomSelections.isEmpty()) {
                // New flow: per-room participant counts
                for (java.util.Map<String, Object> sel : roomSelections) {
                    String roomId = sel.get("roomId") != null ? sel.get("roomId").toString() : null;
                    int roomAdults = sel.get("adults") != null ? Integer.parseInt(sel.get("adults").toString()) : 1;
                    int roomChildren = sel.get("children") != null ? Integer.parseInt(sel.get("children").toString()) : 0;
                    int roomParticipantCount = roomAdults + roomChildren;

                    RoomBookingDetail detail = resolveRoomDetail(roomId, customer, departureDate);
                    TourBookingRequest req = buildRequest(request, detail);
                    req.setParticipantCount(roomParticipantCount); // override with per-room count
                    createdBookingIds.add(tourBookingService.createTourBooking(req));
                    resolvedRooms.add(roomId);
                }
            } else if (legacyRoomNumbers != null && !legacyRoomNumbers.isEmpty()) {
                // Legacy: roomNumbers[] with shared participantCount
                for (String roomNum : legacyRoomNumbers) {
                    RoomBookingDetail detail = resolveRoomDetail(roomNum, customer, departureDate);
                    TourBookingRequest req = buildRequest(request, detail);
                    createdBookingIds.add(tourBookingService.createTourBooking(req));
                    resolvedRooms.add(roomNum);
                }
            } else {
                // No room selected: auto-find an active booking detail for this customer
                RoomBookingDetail autoDetail = null;
                List<RoomBookingDetail> customerDetails = roomBookingDetailRepository.findByCustomer(customer);
                for (RoomBookingDetail rbd : customerDetails) {
                    if (("Confirmed".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus())
                            || "Checked_In".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus()))) {
                        LocalDate checkIn = rbd.getRoomBooking().getCheckInDate();
                        LocalDate checkOut = rbd.getRoomBooking().getCheckOutDate();
                        if (!departureDate.isBefore(checkIn) && !departureDate.isAfter(checkOut)) {
                            autoDetail = rbd;
                            break;
                        }
                    }
                }
                if (autoDetail == null) {
                    throw new IllegalArgumentException("Vui lòng thực hiện đặt phòng trước khi đặt tour.");
                }
                TourBookingRequest req = buildRequest(request, autoDetail);
                createdBookingIds.add(tourBookingService.createTourBooking(req));
                resolvedRooms.add(autoDetail.getCategory() != null ? autoDetail.getCategory().getCategoryName() : "Phòng");
            }

            Long bookingId = createdBookingIds.get(0); // primary booking for VNPay / response

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
            responsePayload.put("roomNumbers", resolvedRooms);
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
            }
        }

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        List<TourAttendee> attendees = tourAttendeeRepository.findByTourBookingId(tb.getId());
        if (attendees != null && !attendees.isEmpty()) {
            for (TourAttendee attendee : attendees) {
                if (attendee.getCustomer() != null) {
                    adultCount++;
                } else if (attendee.getDependent() != null) {
                    Dependent dep = attendee.getDependent();
                    String cccd = dep.getCccdPassportEncrypted();
                    if (cccd != null && cccd.startsWith("AUTO_CHILD_")) {
                        if (cccd.contains("Dưới_2_tuổi")) {
                            infantCount++;
                        } else {
                            childCount++;
                        }
                    } else if (dep.getBirthDate() != null) {
                        int age = java.time.Period.between(dep.getBirthDate(), java.time.LocalDate.now()).getYears();
                        if (age < 2) {
                            infantCount++;
                        } else if (age < 12) {
                            childCount++;
                        } else {
                            adultCount++;
                        }
                    } else {
                        childCount++;
                    }
                } else {
                    adultCount++;
                }
            }
        } else {
            adultCount = tb.getParticipantCount() != null ? tb.getParticipantCount() : 0;
        }

        java.math.BigDecimal totalCharge = tb.getTourCharge() != null ? tb.getTourCharge() : java.math.BigDecimal.ZERO;
        double weight = adultCount + 0.5 * childCount;
        java.math.BigDecimal adultPrice = java.math.BigDecimal.ZERO;
        java.math.BigDecimal childPrice = java.math.BigDecimal.ZERO;

        if (weight > 0) {
            adultPrice = totalCharge.divide(java.math.BigDecimal.valueOf(weight), 2, java.math.RoundingMode.HALF_UP);
            childPrice = adultPrice.multiply(new java.math.BigDecimal("0.5")).setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (tour != null && tour.getBasePrice() != null) {
            adultPrice = tour.getBasePrice();
            childPrice = adultPrice.multiply(new java.math.BigDecimal("0.5")).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        java.math.BigDecimal insuranceFee = java.math.BigDecimal.ZERO;
        String notes = tb.getNotes();
        if (notes != null) {
            String[] parts = notes.split(";");
            for (String part : parts) {
                if (part.startsWith("insuranceFee=")) {
                    try {
                        insuranceFee = new java.math.BigDecimal(part.substring("insuranceFee=".length()).trim());
                    } catch (Exception e) {}
                }
            }
        }

        String insurancePolicyNumber = null;
        if (schedule != null && Boolean.TRUE.equals(schedule.getIsInsuranceProcessed())) {
            insurancePolicyNumber = schedule.getInsurancePolicyNumber();
        }

        data.put("adultCount", adultCount);
        data.put("childCount", childCount);
        data.put("infantCount", infantCount);
        data.put("adultPrice", adultPrice);
        data.put("childPrice", childPrice);
        data.put("infantPrice", java.math.BigDecimal.ZERO);
        data.put("insurancePrice", tour != null && tour.getInsurancePrice() != null ? tour.getInsurancePrice() : new java.math.BigDecimal("50000"));
        data.put("insuranceFee", insuranceFee);
        data.put("isInsuranceRequired", tour != null && Boolean.TRUE.equals(tour.getIsInsuranceRequired()));
        data.put("insurancePolicyNumber", insurancePolicyNumber);

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

    /**
     * Resolve a roomNumber string (Virtual_ID or physical room number) to a RoomBookingDetail.
     */
    private RoomBookingDetail resolveRoomDetail(String roomNum, Customer customer, LocalDate departureDate) {
        if (roomNum == null || roomNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phòng không hợp lệ.");
        }
        RoomBookingDetail detail = null;
        if (roomNum.toUpperCase().startsWith("VIRTUAL_")) {
            try {
                Long detailId = Long.parseLong(roomNum.substring(8));
                detail = roomBookingDetailRepository.findById(detailId).orElse(null);
            } catch (Exception e) {
                throw new IllegalArgumentException("Mã đặt phòng không hợp lệ: " + roomNum);
            }
        } else {
            Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNum);
            if (roomOpt.isEmpty()) {
                throw new IllegalArgumentException("Phòng không hợp lệ: " + roomNum);
            }
            Room room = roomOpt.get();
            if (room.getCurrentBookingDetailId() != null) {
                detail = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId()).orElse(null);
            }
            if (detail == null) {
                List<RoomBookingDetail> customerDetails = roomBookingDetailRepository.findByCustomer(customer);
                for (RoomBookingDetail rbd : customerDetails) {
                    if (rbd.getRoom() != null && rbd.getRoom().getRoomNumber().equals(roomNum)
                            && ("Confirmed".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus())
                                || "Checked_In".equalsIgnoreCase(rbd.getRoomBooking().getBookingStatus()))) {
                        detail = rbd;
                        break;
                    }
                }
            }
        }
        if (detail == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin đặt phòng cho: " + roomNum);
        }
        return detail;
    }

    /**
     * Clone the shared template request and bind it to a specific RoomBookingDetail.
     */
    private TourBookingRequest buildRequest(TourBookingRequest template, RoomBookingDetail detail) {
        TourBookingRequest req = new TourBookingRequest();
        req.setScheduleId(template.getScheduleId());
        req.setCustomerId(template.getCustomerId());
        req.setParticipantCount(template.getParticipantCount());
        req.setChildAges(template.getChildAges());
        req.setPromoCode(template.getPromoCode());
        req.setPostToRoom(template.isPostToRoom());
        req.setPaymentMethod(template.getPaymentMethod());
        req.setVnpPaymentType(template.getVnpPaymentType());
        req.setNotes(template.getNotes());
        req.setWalkInTour(template.isWalkInTour());
        req.setRoomBookingDetailId(detail.getId());
        req.setRoomBookingId(detail.getRoomBooking().getId());
        req.setCompanions(template.getCompanions());
        req.setAcceptInsurance(template.isAcceptInsurance());
        return req;
    }
}
