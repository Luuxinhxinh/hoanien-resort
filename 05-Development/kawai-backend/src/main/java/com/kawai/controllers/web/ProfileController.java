package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.kawai.utils.EncryptionUtils;
import com.kawai.services.impl.FileUploadService;

import com.kawai.models.RoomBooking;
import com.kawai.models.TourBooking;
import com.kawai.models.FoodOrder;
import com.kawai.models.FoodOrderDetail;
import com.kawai.models.RoomBookingDetail;
import java.util.List;
import java.util.Collections;

import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.TourBookingRepository;
import com.kawai.repositories.FoodOrderRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private String extractUsername(Authentication authentication) {
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
            String email = oauthToken.getPrincipal().getAttribute("email");
            if (email != null) {
                return email;
            }
        }
        return authentication.getName();
    }

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoomBookingRepository roomBookingRepository;

    @Autowired
    private com.kawai.repositories.BookingRepository bookingRepository;

    @Autowired
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private com.kawai.repositories.DependentRepository dependentRepository;

    @Autowired
    private com.kawai.repositories.RoomGuestRepository roomGuestRepository;

    @Autowired
    private com.kawai.services.interfaces.TourBookingService tourBookingService;

    @Autowired
    private com.kawai.services.interfaces.EmailService emailService;

    @Autowired
    private com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private com.kawai.repositories.TableReservationRepository tableReservationRepository;

    @Autowired
    private com.kawai.repositories.FolioItemRepository folioItemRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer != null && customer.getMembershipTier() == null) {
            com.kawai.models.MembershipTier defaultTier = new com.kawai.models.MembershipTier();
            defaultTier.setTierName("REGULAR");
            defaultTier.setCreditLimit(new java.math.BigDecimal("5000000.00"));
            customer.setMembershipTier(defaultTier);
        }
        model.addAttribute("customer", customer);

        // Safe calculations for Membership Tier, Loyalty Points and UI Progress to
        // avoid Thymeleaf translation exceptions
        String tierName = "REGULAR";
        int pts = 0;
        int maxPts = 1000;
        int percent = 0;
        String nextTierInfo = "Max Tier Reached!";
        String avatarInitial = "G";

        if (customer != null) {
            try {
                if (customer.getMembershipTier() != null) {
                    tierName = customer.getMembershipTier().getTierName();
                }
            } catch (Exception e) {
                tierName = "REGULAR";
            }
            if (customer.getLoyaltyPoints() != null) {
                pts = customer.getLoyaltyPoints();
            }
            if (customer.getFullName() != null && !customer.getFullName().trim().isEmpty()) {
                avatarInitial = customer.getFullName().trim().substring(0, 1).toUpperCase();
            }
        }

        if (pts < 1000) {
            maxPts = 1000;
            nextTierInfo = (1000 - pts) + " pts to Silver";
        } else if (pts < 5000) {
            maxPts = 5000;
            nextTierInfo = (5000 - pts) + " pts to Gold";
        } else if (pts < 10000) {
            maxPts = 10000;
            nextTierInfo = (10000 - pts) + " pts to Platinum";
        } else {
            maxPts = pts;
            nextTierInfo = "Hạng thẻ cao nhất!";
        }

        percent = maxPts > 0 ? (pts * 100 / maxPts) : 0;
        if (percent > 100)
            percent = 100;

        model.addAttribute("tierName", tierName != null ? tierName.toUpperCase() : "REGULAR");
        model.addAttribute("loyaltyPoints", pts);
        model.addAttribute("progressPercent", percent);
        model.addAttribute("nextTierInfo", nextTierInfo);
        model.addAttribute("avatarInitial", avatarInitial);

        if (customer != null) {
            // Get bookings where customer is master
            List<RoomBooking> masterBookings = roomBookingRepository.findByCustomerOrderByIdDesc(customer);
            // Get details where customer is occupant
            List<RoomBookingDetail> occupantDetails = roomBookingDetailRepository.findByCustomer(customer);

            java.util.Set<RoomBooking> uniqueBookings = new java.util.HashSet<>(masterBookings);
            for (RoomBookingDetail d : occupantDetails) {
                if (d.getRoomBooking() != null) {
                    uniqueBookings.add(d.getRoomBooking());
                }
            }

            List<RoomBooking> roomBookings = uniqueBookings.stream()
                    .filter(b -> {
                        String status = b.getBookingStatus() != null ? b.getBookingStatus().toUpperCase() : "";

                        if (status.equals("PENDING") || status.equals("PENDING_PAYMENT")) {
                            return false;
                        }

                        return true;
                    })
                    .sorted((b1, b2) -> b2.getId().compareTo(b1.getId()))
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("bookings", roomBookings);

            java.util.Map<Long, List<RoomBookingDetail>> visibleDetailsMap = new java.util.HashMap<>();
            java.util.Map<Long, java.math.BigDecimal> remainingLimits = new java.util.HashMap<>();

            java.util.Map<Long, List<com.kawai.models.FolioItem>> folioItemsMap = new java.util.HashMap<>();
            java.util.Map<Long, List<com.kawai.models.RoomGuest>> roomGuestsMap = new java.util.HashMap<>();
            java.util.Map<Long, Boolean> hasAttachedToursMap = new java.util.HashMap<>();
            java.util.Map<Long, Boolean> hasRefundableItemsMap = new java.util.HashMap<>();
            List<RoomBooking> activeStays = new java.util.ArrayList<>();

            for (RoomBooking rb : roomBookings) {
                if ("CHECKED_IN".equalsIgnoreCase(rb.getBookingStatus())) {
                    activeStays.add(rb);
                }

                populateRefundInfo(rb, hasAttachedToursMap, hasRefundableItemsMap);

                List<RoomBookingDetail> allDetails = roomBookingDetailRepository.findByRoomBookingId(rb.getId());
                List<RoomBookingDetail> visibleDetails;

                if (rb.getCustomer() != null && rb.getCustomer().getId().equals(customer.getId())) {
                    visibleDetails = allDetails; // Master sees all
                } else {
                    visibleDetails = allDetails.stream()
                            .filter(d -> d.getCustomer() != null && d.getCustomer().getId().equals(customer.getId()))
                            .collect(java.util.stream.Collectors.toList());
                }

                visibleDetailsMap.put(rb.getId(), visibleDetails);

                for (RoomBookingDetail d : visibleDetails) {
                    java.math.BigDecimal subLimit = (d.getSubCreditLimit() != null && d.getSubCreditLimit().compareTo(java.math.BigDecimal.ZERO) > 0)
                            ? d.getSubCreditLimit()
                            : (rb.getCreditLimit() != null && rb.getCreditLimit().compareTo(java.math.BigDecimal.ZERO) > 0
                                    ? rb.getCreditLimit()
                                    : new java.math.BigDecimal("5000000.00"));
                    List<com.kawai.models.FolioItem> folioItems = folioItemRepository
                            .findByRoomBookingDetailId(d.getId());

                    folioItemsMap.put(d.getId(), folioItems);
                    roomGuestsMap.put(d.getId(), roomGuestRepository.findByRoomBookingDetailId(d.getId()));

                    java.math.BigDecimal spent = folioItems.stream()
                            .filter(f -> !Boolean.TRUE.equals(f.getIsSettledSeparately()))
                            .map(com.kawai.models.FolioItem::getAmount)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                    remainingLimits.put(d.getId(), subLimit.subtract(spent));
                }
            }
            model.addAttribute("visibleDetailsMap", visibleDetailsMap);
            model.addAttribute("remainingLimits", remainingLimits);
            model.addAttribute("folioItemsMap", folioItemsMap);
            model.addAttribute("roomGuestsMap", roomGuestsMap);
            model.addAttribute("activeStays", activeStays);
            model.addAttribute("hasAttachedToursMap", hasAttachedToursMap);
            model.addAttribute("hasRefundableItemsMap", hasRefundableItemsMap);

            List<TourBooking> tourBookings = bookingRepository.findByCustomerId(customer.getId()).stream()
                    .filter(b -> b instanceof TourBooking)
                    .map(b -> (TourBooking) b)
                    .filter(tb -> {
                        String status = tb.getBookingStatus() != null ? tb.getBookingStatus().toUpperCase() : "";

                        if (status.equals("PENDING") || status.equals("PENDING_PAYMENT")) {
                            return false;
                        }

                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            for (TourBooking tb : tourBookings) {
                if (tb.getSchedule() != null) {
                    tb.getSchedule().getDepartureDate();
                    if (tb.getSchedule().getTour() != null) {
                        tb.getSchedule().getTour().getTourName();
                    }
                }
            }
            model.addAttribute("tourBookings", tourBookings);

            List<FoodOrder> foodOrders = foodOrderRepository.findByCustomer(customer);
            for (FoodOrder fo : foodOrders) {
                if (fo.getDetails() != null) {
                    fo.getDetails().size();
                    for (FoodOrderDetail detail : fo.getDetails()) {
                        if (detail.getMenuItem() != null) {
                            detail.getMenuItem().getItemName();
                        }
                    }
                }
            }
            model.addAttribute("foodOrders", foodOrders);

            List<com.kawai.models.Dependent> dependents = dependentRepository.findByCustomer(customer).stream()
                    .filter(d -> !"Khách đi kèm".equalsIgnoreCase(d.getDependentName()))
                    .filter(d -> d.getIsDeleted() == null || !d.getIsDeleted())
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("dependents", dependents);

            List<com.kawai.models.TableReservation> tableReservations = tableReservationRepository
                    .findByCustomerOrderByIdDesc(customer);
            model.addAttribute("tableReservations", tableReservations);
        } else {
            model.addAttribute("bookings", Collections.emptyList());
            model.addAttribute("bookingFirstDetails", Collections.emptyMap());
            model.addAttribute("tourBookings", Collections.emptyList());
            model.addAttribute("foodOrders", Collections.emptyList());
            model.addAttribute("dependents", Collections.emptyList());
            model.addAttribute("tableReservations", Collections.emptyList());
        }
        return "guest/profile";
    }

    @GetMapping("/bookings")
    public String viewBookingHistory(Authentication authentication, Model model,
            @org.springframework.web.bind.annotation.RequestParam(value = "payment", required = false) String paymentStatus) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }

        String redirectUrl = "redirect:/profile#bookings";
        if (paymentStatus != null) {
            redirectUrl += "?payment=" + paymentStatus;
        }
        return redirectUrl;
    }

    @GetMapping({ "/update", "/edit" })
    public String redirectProfile() {
        return "redirect:/profile";
    }

    @PostMapping({ "/edit", "/update" })
    public String editProfile(Authentication authentication,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String cccd,
            RedirectAttributes redirectAttributes) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer != null) {
            customer.setFullName(fullName);
            if (email != null && !email.trim().isEmpty()) {
                customer.setEmail(email.trim());
            }
            if (gender != null) {
                customer.setGender(gender);
            }
            if (phone != null && !phone.trim().isEmpty()) {
                if (!com.kawai.utils.ValidationUtils.isValidPhone(phone)) {
                    redirectAttributes.addFlashAttribute("error",
                            "Số điện thoại không hợp lệ (Phải gồm 10 số và bắt đầu bằng 0)");
                    return "redirect:/profile";
                }
                customer.setPhone(phone);
            }
            if (cccd != null && !cccd.trim().isEmpty() && !"********".equals(cccd)) {
                if (!com.kawai.utils.ValidationUtils.isValidDocument(cccd)) {
                    redirectAttributes.addFlashAttribute("error",
                            "CCCD/Passport không hợp lệ (Phải là CCCD 12 số, hoặc Passport 8-12 ký tự có chứa chữ cái)");
                    return "redirect:/profile";
                }
                customer.setCccdPassportEncrypted(EncryptionUtils.encrypt(cccd.trim()));
            }
            customerRepository.save(customer);

            // Gửi email thông báo cập nhật thành công
            if (emailService != null) {
                emailService.sendProfileUpdateEmail(customer);
            }

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Account account = accountRepository.findByUsername(username).orElse(null);

        if (account != null) {
            if (passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
                account.setPasswordHash(passwordEncoder.encode(newPassword));
                accountRepository.save(account);
                redirectAttributes.addFlashAttribute("success", "Äá»•i máº­t kháº©u thÃ nh cÃ´ng!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không chính xác!");
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/dependents/add")
    public String addDependent(Authentication authentication,
            @RequestParam String dependentName,
            @RequestParam String gender,
            @RequestParam String birthDate,
            @RequestParam(required = false) String cccd,
            RedirectAttributes redirectAttributes) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer != null) {
            // Ngăn chặn spam data: Giới hạn mỗi khách hàng chỉ được lưu tối đa 40 người đi
            // cùng
            List<com.kawai.models.Dependent> dependents = dependentRepository.findByCustomer(customer).stream()
                    .filter(d -> !"Khách đi kèm".equalsIgnoreCase(d.getDependentName()))
                    .filter(d -> d.getIsDeleted() == null || !d.getIsDeleted())
                    .collect(java.util.stream.Collectors.toList());
            long currentDependentsCount = dependents.size();
            if (currentDependentsCount >= 40) {
                redirectAttributes.addFlashAttribute("error",
                        "Danh bạ của bạn đã đầy. Bạn chỉ được phép lưu tối đa 40 người đi cùng!");
                return "redirect:/profile";
            }

            com.kawai.models.Dependent dep = new com.kawai.models.Dependent();
            dep.setCustomer(customer);
            dep.setDependentName(dependentName);
            dep.setGender(gender);
            try {
                dep.setBirthDate(java.time.LocalDate.parse(birthDate));
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Ngày sinh không đúng định dạng!");
                return "redirect:/profile";
            }
            if (cccd != null && !cccd.trim().isEmpty()) {
                if (!com.kawai.utils.ValidationUtils.isValidCccd(cccd)) {
                    redirectAttributes.addFlashAttribute("error",
                            "CCCD/Passport của người đi cùng không hợp lệ (Phải là CCCD 12 số, hoặc Passport 8-12 ký tự có chứa chữ cái)");
                    return "redirect:/profile";
                }
                dep.setCccdPassportEncrypted(EncryptionUtils.encrypt(cccd.trim()));
            }
            dependentRepository.save(dep);
            redirectAttributes.addFlashAttribute("success", "Thêm người đi cùng thành công!");
        }
        return "redirect:/profile";
    }

    @PostMapping("/dependents/delete/{id}")
    public String deleteDependent(Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer != null) {
            com.kawai.models.Dependent dep = dependentRepository.findById(id).orElse(null);
            if (dep != null && dep.getCustomer().getId().equals(customer.getId())) {
                try {
                    dep.setIsDeleted(true);
                    dependentRepository.save(dep);
                    redirectAttributes.addFlashAttribute("success", "Xóa người đi cùng thành công!");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error",
                            "Không thể xóa người đi cùng vì lỗi hệ thống!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người đi cùng!");
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(
            @org.springframework.web.bind.annotation.RequestParam("avatarFile") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));

        if (customer != null && !file.isEmpty()) {
            try {
                // Upload file vào thư mục "hoanien_avatars" trên Cloudinary
                String secureUrl = fileUploadService.uploadFile(file, "hoanien_avatars");

                customer.setAvatarUrl(secureUrl);
                customerRepository.save(customer);

                redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi tải lên ảnh đại diện: " + e.getMessage());
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/tours/cancel/{bookingId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> cancelTour(
            @PathVariable Long bookingId,
            @org.springframework.web.bind.annotation.RequestBody(required = false) com.kawai.dto.CancelBookingRequestDTO dto,
            Authentication authentication) {
        if (!com.kawai.utils.SecurityUtils.isCustomerLoggedIn(authentication)) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("status", "error", "message", "Vui lòng đăng nhập để thực hiện thao tác!"));
        }

        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));

        if (customer == null) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("status", "error", "message", "Không tìm thấy thông tin khách hàng!"));
        }

        TourBooking booking = tourBookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("status", "error", "message", "Không tìm thấy đơn đặt tour!"));
        }

        // Kiểm tra quyền sở hữu đơn đặt tour
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("status", "error", "message", "Bạn không có quyền hủy đơn đặt tour này!"));
        }

        // Kiểm tra trạng thái hiện tại (Chỉ cho phép hủy nếu là CONFIRMED hoặc PENDING)
        String status = booking.getBookingStatus() != null ? booking.getBookingStatus().toUpperCase() : "";
        if (!"CONFIRMED".equals(status) && !"PENDING".equals(status)) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("status", "error", "message",
                            "Đơn tour này không thể hủy (Trạng thái hiện tại: " + booking.getBookingStatus() + ")!"));
        }

        try {
            java.math.BigDecimal refundAmount = tourBookingService.cancelTourByCustomer(bookingId, customer.getId(),
                    dto);

            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "Đã hủy đơn đặt tour thành công!",
                    "refundAmount", refundAmount));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("status", "error", "message",
                            "Lỗi hệ thống khi hủy tour: " + e.getMessage()));
        }
    }

    private void populateRefundInfo(RoomBooking rb, java.util.Map<Long, Boolean> hasAttachedToursMap,
            java.util.Map<Long, Boolean> hasRefundableItemsMap) {
        boolean roomRefundable = rb.getCancellationDeadline() != null
                && !java.time.LocalDateTime.now().isAfter(rb.getCancellationDeadline());
        List<TourBooking> attachedTours = tourBookingRepository.findByRoomBookingId(rb.getId());
        boolean hasTours = !attachedTours.isEmpty();
        boolean anyTourRefundable = false;

        for (TourBooking tb : attachedTours) {
            if (tb.getSchedule() != null && tb.getSchedule().getDepartureDate() != null) {
                java.time.LocalDateTime depTime = tb.getSchedule().getDepartureDate().atTime(
                        tb.getSchedule().getDepartureTime() != null ? tb.getSchedule().getDepartureTime()
                                : java.time.LocalTime.of(7, 0));
                if (java.time.temporal.ChronoUnit.HOURS.between(java.time.LocalDateTime.now(), depTime) > 24) {
                    anyTourRefundable = true;
                    break;
                }
            }
        }
        hasAttachedToursMap.put(rb.getId(), hasTours);
        hasRefundableItemsMap.put(rb.getId(), roomRefundable || anyTourRefundable);
    }
}
