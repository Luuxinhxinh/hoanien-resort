package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private com.kawai.repositories.CustomerRepository customerRepository;

    @Autowired
    private com.kawai.repositories.TourBookingRepository tourBookingRepository;

    @Autowired
    private com.kawai.repositories.ReviewRepository reviewRepository;

    @Autowired
    private com.kawai.repositories.BookingRepository bookingRepository;

    @Autowired
    private com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private com.kawai.repositories.TourStaffAssignmentRepository tourStaffAssignmentRepository;

    @GetMapping
    public String showFeedbackPage(
            Principal principal,
            jakarta.servlet.http.HttpSession session,
            @RequestParam(name = "bookingId", required = false) Long bookingId,
            @RequestParam(name = "type", required = false, defaultValue = "all") String type,
            Model model) {

        boolean isLoggedIn = com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal)
                || (session != null && session.getAttribute("user") != null);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("activeTab", type);

        // Mode: submit form (came from email with bookingId)
        com.kawai.models.TourBooking booking = null;
        if (bookingId != null) {
            if (reviewRepository.existsByTourBookingId(bookingId)) {
                return "redirect:/feedback?toast=already_reviewed";
            }
            booking = tourBookingRepository.findById(bookingId).orElse(null);
        }

        com.kawai.models.Customer customer = null;
        boolean hasUsedService = false;

        if (isLoggedIn) {
            String username = null;
            if (principal != null) {
                username = principal.getName();
                if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                    username = oauthToken.getPrincipal().getAttribute("email");
                }
            } else if (session != null && session.getAttribute("user") != null) {
                com.kawai.models.Account sessionUser = (com.kawai.models.Account) session.getAttribute("user");
                username = sessionUser.getUsername();
            }

            if (booking != null) {
                customer = booking.getCustomer();
            } else if (username != null) {
                final String effectiveUsername = username;
                customer = customerRepository.findByAccount_Username(effectiveUsername)
                        .or(() -> customerRepository.findByEmail(effectiveUsername))
                        .orElse(null);
            }

            // Fallback cho Admin/Staff demo
            if (customer == null) {
                System.out.println("DEBUG FEEDBACK (GET): Tai khoan la Staff/Admin. Dung Customer ID = 1 lam fallback de demo.");
                customer = customerRepository.findById(1L).orElse(null);
            }

            if (customer != null) {
                long completedCount = bookingRepository.countCompletedOrConfirmedBookingsByCustomerId(customer.getId());
                hasUsedService = completedCount > 0;
                
                // Lấy các TourBookings chưa đánh giá
                List<com.kawai.models.TourBooking> allTours = tourBookingRepository.findTourBookingsByCustomerId(customer.getId());
                List<com.kawai.models.TourBooking> unreviewedTours = new java.util.ArrayList<>();
                if (allTours != null) {
                    for (com.kawai.models.TourBooking tb : allTours) {
                        if (!reviewRepository.existsByTourBookingId(tb.getId())) {
                            unreviewedTours.add(tb);
                        }
                    }
                }
                model.addAttribute("unreviewedTours", unreviewedTours);

                // Lấy các RoomBookingDetails chưa đánh giá
                List<com.kawai.models.RoomBookingDetail> allRooms = roomBookingDetailRepository.findByAnyCustomerId(customer.getId());
                List<com.kawai.models.RoomBookingDetail> unreviewedRooms = new java.util.ArrayList<>();
                if (allRooms != null) {
                    for (com.kawai.models.RoomBookingDetail rd : allRooms) {
                        if (!reviewRepository.existsByRoomBookingDetailId(rd.getId())) {
                            unreviewedRooms.add(rd);
                        }
                    }
                }
                model.addAttribute("unreviewedRooms", unreviewedRooms);
            }
        }

        model.addAttribute("hasUsedService", hasUsedService);

        if (booking != null) {
            model.addAttribute("booking", booking);
            String rawTourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName() : "Hành trình trải nghiệm";
            String normalizedTourName = java.text.Normalizer.normalize(rawTourName, java.text.Normalizer.Form.NFC);
            model.addAttribute("tourName", normalizedTourName);
            model.addAttribute("mode", "submit");
        } else {
            model.addAttribute("booking", null);
            model.addAttribute("tourName", null);
            model.addAttribute("mode", "browse");
        }

        // --- 1. Tour Reviews ---
        List<com.kawai.models.Review> tourReviews = new java.util.ArrayList<>();
        List<com.kawai.models.Review> dbTourReviews = safeLoad(() -> reviewRepository.findApprovedTourReviews());
        System.out.println("DEBUG FEEDBACK: Load tour reviews tu DB - count=" + (dbTourReviews != null ? dbTourReviews.size() : 0));
        if (dbTourReviews != null) {
            for (com.kawai.models.Review r : dbTourReviews) {
                if (r.getTourBooking() != null && r.getTourBooking().getSchedule() != null) {
                    if (r.getTourBooking().getSchedule().getTour() != null) {
                        r.setTourNameResolved(r.getTourBooking().getSchedule().getTour().getTourName());
                    }
                    Long scheduleId = r.getTourBooking().getSchedule().getId();
                    List<com.kawai.models.TourStaffAssignment> assigns = tourStaffAssignmentRepository.findByScheduleId(scheduleId);
                    if (assigns != null && !assigns.isEmpty()) {
                        com.kawai.models.TourStaffAssignment lead = assigns.stream()
                            .filter(a -> Boolean.TRUE.equals(a.getIsLeadGuide()))
                            .findFirst()
                            .orElse(assigns.get(0));
                        if (lead.getEmployee() != null) {
                            r.setGuideName(lead.getEmployee().getFullName());
                        }
                    }
                }
                System.out.println("DEBUG FEEDBACK: reviewId=" + r.getId() + ", customer=" + (r.getCustomer() != null ? r.getCustomer().getFullName() : "null") + ", tourName=" + r.getTourNameResolved() + ", guide=" + r.getGuideName());
            }
            tourReviews.addAll(dbTourReviews);
        }
        tourReviews.sort((r1, r2) -> {
            java.time.LocalDateTime time1 = r1.getCreatedAt() != null ? r1.getCreatedAt() : java.time.LocalDateTime.MIN;
            java.time.LocalDateTime time2 = r2.getCreatedAt() != null ? r2.getCreatedAt() : java.time.LocalDateTime.MIN;
            return time2.compareTo(time1);
        });

        // --- 2. Room Reviews ---
        List<com.kawai.models.Review> roomReviews = new java.util.ArrayList<>();
        List<com.kawai.models.Review> dbRoomReviews = safeLoad(() -> reviewRepository.findApprovedRoomReviews());
        if (dbRoomReviews != null) {
            roomReviews.addAll(dbRoomReviews);
        }
        roomReviews.sort((r1, r2) -> {
            java.time.LocalDateTime time1 = r1.getCreatedAt() != null ? r1.getCreatedAt() : java.time.LocalDateTime.MIN;
            java.time.LocalDateTime time2 = r2.getCreatedAt() != null ? r2.getCreatedAt() : java.time.LocalDateTime.MIN;
            return time2.compareTo(time1);
        });

        // --- 3. General Reviews ---
        List<com.kawai.models.Review> generalReviews = new java.util.ArrayList<>();
        List<com.kawai.models.Review> dbGenReviews = safeLoad(() -> reviewRepository.findApprovedGeneralReviews());
        if (dbGenReviews != null) {
            generalReviews.addAll(dbGenReviews);
        }
        generalReviews.sort((r1, r2) -> {
            java.time.LocalDateTime time1 = r1.getCreatedAt() != null ? r1.getCreatedAt() : java.time.LocalDateTime.MIN;
            java.time.LocalDateTime time2 = r2.getCreatedAt() != null ? r2.getCreatedAt() : java.time.LocalDateTime.MIN;
            return time2.compareTo(time1);
        });

        model.addAttribute("tourReviews", tourReviews);
        model.addAttribute("roomReviews", roomReviews);
        model.addAttribute("generalReviews", generalReviews);

        return "guest/feedback";
    }

    @PostMapping("/submit")
    public String submitFeedback(
            Principal principal,
            jakarta.servlet.http.HttpSession session,
            @RequestParam(name = "bookingId", required = false) Long bookingId,
            @RequestParam(name = "selectedTourBookingId", required = false) Long selectedTourBookingId,
            @RequestParam(name = "selectedRoomBookingDetailId", required = false) Long selectedRoomBookingDetailId,
            @RequestParam(name = "ratingTour", required = false) Integer ratingTour,
            @RequestParam(name = "ratingRoomDining", required = false) Integer ratingRoomDining,
            @RequestParam(name = "ratingService", required = false) Integer ratingService,
            @RequestParam("reviewText") String reviewText) {

        boolean isLoggedIn = com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal)
                || (session != null && session.getAttribute("user") != null);

        Long targetTourBookingId = bookingId != null ? bookingId : selectedTourBookingId;
        if (!isLoggedIn) {
            String redirectUrl = "/feedback?toast=login_required";
            if (targetTourBookingId != null) {
                redirectUrl += "&bookingId=" + targetTourBookingId;
            }
            return "redirect:" + redirectUrl;
        }
        if (targetTourBookingId != null && reviewRepository.existsByTourBookingId(targetTourBookingId)) {
            System.out.println("DEBUG FEEDBACK: Chan submit - targetTourBookingId " + targetTourBookingId + " da duoc danh gia.");
            return "redirect:/feedback?toast=already_reviewed";
        }

        if (selectedRoomBookingDetailId != null && reviewRepository.existsByRoomBookingDetailId(selectedRoomBookingDetailId)) {
            System.out.println("DEBUG FEEDBACK: Chan submit - selectedRoomBookingDetailId " + selectedRoomBookingDetailId + " da duoc danh gia.");
            return "redirect:/feedback?toast=already_reviewed";
        }

        com.kawai.models.TourBooking booking = null;
        com.kawai.models.RoomBookingDetail roomDetail = null;
        com.kawai.models.Customer customer = null;

        if (targetTourBookingId != null) {
            booking = tourBookingRepository.findById(targetTourBookingId).orElse(null);
            if (booking != null) {
                customer = booking.getCustomer();
            }
        }

        if (selectedRoomBookingDetailId != null) {
            roomDetail = roomBookingDetailRepository.findById(selectedRoomBookingDetailId).orElse(null);
            if (roomDetail != null && roomDetail.getRoomBooking() != null && customer == null) {
                customer = roomDetail.getRoomBooking().getCustomer();
            }
        }

        if (customer == null) {
            String username = null;
            if (principal != null) {
                username = principal.getName();
                if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                    username = oauthToken.getPrincipal().getAttribute("email");
                }
            } else if (session != null && session.getAttribute("user") != null) {
                com.kawai.models.Account sessionUser = (com.kawai.models.Account) session.getAttribute("user");
                username = sessionUser.getUsername();
            }

            if (username != null) {
                final String effectiveUsername = username;
                customer = customerRepository.findByAccount_Username(effectiveUsername)
                        .or(() -> customerRepository.findByEmail(effectiveUsername))
                        .orElse(null);
            }

            // Fallback cho Admin/Staff demo
            if (customer == null) {
                System.out.println("DEBUG FEEDBACK (POST): Tai khoan la Staff/Admin. Dung Customer ID = 1 lam fallback de demo.");
                customer = customerRepository.findById(1L).orElse(null);
            }
        }

        if (customer == null) {
            return "redirect:/feedback?toast=error";
        }

        // Check if customer has used any services (chỉ chặn nếu không có booking hoàn thành)
        long completedCount = bookingRepository.countCompletedOrConfirmedBookingsByCustomerId(customer.getId());
        if (completedCount <= 0 && targetTourBookingId == null && selectedRoomBookingDetailId == null) {
            System.out.println("DEBUG FEEDBACK: Chan submit - Customer khong co booking hoan thanh");
            return "redirect:/feedback?toast=no_service_used";
        }

        if (targetTourBookingId == null && ratingTour != null) {
            // Khách hàng gửi đánh giá tour chung -> tự động liên kết với TourBooking gần đây nhất của họ chưa đánh giá
            java.util.List<com.kawai.models.TourBooking> tourBookings = tourBookingRepository.findTourBookingsByCustomerId(customer.getId());
            if (tourBookings != null && !tourBookings.isEmpty()) {
                tourBookings.sort((b1, b2) -> {
                    Long id1 = b1.getId() != null ? b1.getId() : 0L;
                    Long id2 = b2.getId() != null ? b2.getId() : 0L;
                    return id2.compareTo(id1);
                });
                for (com.kawai.models.TourBooking tb : tourBookings) {
                    if (!reviewRepository.existsByTourBookingId(tb.getId())) {
                        booking = tb;
                        break;
                    }
                }
                if (booking == null) {
                    System.out.println("DEBUG FEEDBACK: Chan submit - Toan bo TourBookings cua khach hang da duoc review.");
                    return "redirect:/feedback?toast=already_reviewed";
                }
            }
        }

        if (selectedRoomBookingDetailId == null && ratingRoomDining != null) {
            // Khách hàng gửi đánh giá phòng chung -> tự động liên kết với RoomBookingDetail gần đây nhất của họ chưa đánh giá
            java.util.List<com.kawai.models.RoomBookingDetail> roomDetails = roomBookingDetailRepository.findByAnyCustomerId(customer.getId());
            if (roomDetails != null && !roomDetails.isEmpty()) {
                roomDetails.sort((r1, r2) -> {
                    Long id1 = r1.getId() != null ? r1.getId() : 0L;
                    Long id2 = r2.getId() != null ? r2.getId() : 0L;
                    return id2.compareTo(id1);
                });
                for (com.kawai.models.RoomBookingDetail rd : roomDetails) {
                    if (!reviewRepository.existsByRoomBookingDetailId(rd.getId())) {
                        roomDetail = rd;
                        break;
                    }
                }
                if (roomDetail == null) {
                    System.out.println("DEBUG FEEDBACK: Chan submit - Toan bo RoomBookingDetails cua khach hang da duoc review.");
                    return "redirect:/feedback?toast=already_reviewed";
                }
            }
        }

        com.kawai.models.Review review = new com.kawai.models.Review();
        review.setCustomer(customer);
        review.setTourBooking(booking);
        review.setRoomBookingDetail(roomDetail);
        review.setRatingTour(ratingTour);
        review.setRatingRoomDining(ratingRoomDining);
        
        int finalServiceRating = 5;
        if (ratingService != null) {
            finalServiceRating = ratingService;
        } else if (ratingTour != null && ratingRoomDining != null) {
            finalServiceRating = (ratingTour + ratingRoomDining) / 2;
        } else if (ratingTour != null) {
            finalServiceRating = ratingTour;
        } else if (ratingRoomDining != null) {
            finalServiceRating = ratingRoomDining;
        }
        review.setRatingService(finalServiceRating);
        review.setReviewText(reviewText);
        review.setCreatedAt(LocalDateTime.now());
        review.setModerationStatus("Approved"); // Auto-approved for demo

        System.out.println("DEBUG FEEDBACK: Ghi nhận submit - customerId=" + customer.getId() + ", name=" + customer.getFullName() + ", bookingId=" + (booking != null ? booking.getId() : "null") + ", tourRating=" + ratingTour + ", text=" + reviewText);
        com.kawai.models.Review savedReview = reviewRepository.save(review);
        System.out.println("DEBUG FEEDBACK: Lưu thành công reviewId=" + savedReview.getId() + ", moderationStatus=" + savedReview.getModerationStatus());

        return "redirect:/feedback?toast=success&type=tour";
    }

    private <T> List<T> safeLoad(java.util.function.Supplier<List<T>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
