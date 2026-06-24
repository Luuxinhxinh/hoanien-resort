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
    private TourBookingRepository tourBookingRepository;

    @Autowired
    private FoodOrderRepository foodOrderRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private com.kawai.repositories.DependentRepository dependentRepository;

    @Autowired
    private com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private com.kawai.repositories.TableReservationRepository tableReservationRepository;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        model.addAttribute("customer", customer);

        if (customer != null) {
            List<RoomBooking> roomBookings = roomBookingRepository.findByCustomerOrderByIdDesc(customer)
                    .stream()
                    .filter(b -> {
                        if ("HOLD".equalsIgnoreCase(b.getBookingStatus())) {
                            return false;
                        }
                        if (b.getBookingStatus() != null && b.getBookingStatus().toUpperCase().startsWith("CANCEL")) {
                            return paymentTransactionRepository.existsByBookingIdAndStatus(b.getId(),
                                    com.kawai.models.PaymentStatus.SUCCESS);
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("roomBookings", roomBookings);

            List<TourBooking> tourBookings = tourBookingRepository.findByCustomer(customer);
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

            List<com.kawai.models.Dependent> dependents = dependentRepository.findByCustomer(customer);
            model.addAttribute("dependents", dependents);

            List<com.kawai.models.TableReservation> tableReservations = tableReservationRepository
                    .findByCustomerOrderByIdDesc(customer);
            model.addAttribute("tableReservations", tableReservations);
        } else {
            model.addAttribute("roomBookings", Collections.emptyList());
            model.addAttribute("tourBookings", Collections.emptyList());
            model.addAttribute("foodOrders", Collections.emptyList());
            model.addAttribute("dependents", Collections.emptyList());
            model.addAttribute("tableReservations", Collections.emptyList());
        }
        return "guest/profile";
    }

    @GetMapping("/bookings")
    public String viewBookingHistory(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        model.addAttribute("customer", customer);

        if (customer != null) {
            List<RoomBooking> roomBookings = roomBookingRepository.findByCustomerOrderByIdDesc(customer)
                    .stream()
                    .filter(b -> {
                        if ("HOLD".equalsIgnoreCase(b.getBookingStatus())) {
                            return false;
                        }
                        if (b.getBookingStatus() != null && b.getBookingStatus().toUpperCase().startsWith("CANCEL")) {
                            return paymentTransactionRepository.existsByBookingIdAndStatus(b.getId(),
                                    com.kawai.models.PaymentStatus.SUCCESS);
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("bookings", roomBookings);

            java.util.Map<Long, RoomBookingDetail> bookingFirstDetails = new java.util.HashMap<>();
            for (RoomBooking rb : roomBookings) {
                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(rb.getId());
                if (!details.isEmpty()) {
                    bookingFirstDetails.put(rb.getId(), details.get(0));
                }
            }
            model.addAttribute("bookingFirstDetails", bookingFirstDetails);

            // Fetch and initialize tourBookings
            List<TourBooking> tourBookings = tourBookingRepository.findByCustomer(customer);
            for (TourBooking tb : tourBookings) {
                if (tb.getSchedule() != null) {
                    tb.getSchedule().getDepartureDate();
                    if (tb.getSchedule().getTour() != null) {
                        tb.getSchedule().getTour().getTourName();
                    }
                }
            }
            model.addAttribute("tourBookings", tourBookings);

            // Fetch and initialize foodOrders
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

            List<com.kawai.models.TableReservation> tableReservations = tableReservationRepository
                    .findByCustomerOrderByIdDesc(customer);
            model.addAttribute("tableReservations", tableReservations);
        } else {
            model.addAttribute("bookings", Collections.emptyList());
            model.addAttribute("bookingFirstDetails", Collections.emptyMap());
            model.addAttribute("tourBookings", Collections.emptyList());
            model.addAttribute("foodOrders", Collections.emptyList());
            model.addAttribute("tableReservations", Collections.emptyList());
        }
        return "guest/booking-history";
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
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
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
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
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
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer != null) {
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
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/booking";
        }
        String username = extractUsername(authentication);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer != null) {
            com.kawai.models.Dependent dep = dependentRepository.findById(id).orElse(null);
            if (dep != null && dep.getCustomer().getId().equals(customer.getId())) {
                dependentRepository.delete(dep);
                redirectAttributes.addFlashAttribute("success", "Xóa người đi cùng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người đi cùng!");
            }
        }
        return "redirect:/profile";
    }
}
