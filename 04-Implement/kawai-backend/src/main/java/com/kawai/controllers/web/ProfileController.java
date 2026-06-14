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

import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.TourBookingRepository;
import com.kawai.repositories.FoodOrderRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {

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

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
        model.addAttribute("customer", customer);
        
        if (customer != null) {
            model.addAttribute("roomBookings", roomBookingRepository.findByCustomer(customer));
            model.addAttribute("tourBookings", tourBookingRepository.findByCustomer(customer));
            model.addAttribute("foodOrders", foodOrderRepository.findByBooking_Customer(customer));
        } else {
            model.addAttribute("roomBookings", java.util.Collections.emptyList());
            model.addAttribute("tourBookings", java.util.Collections.emptyList());
            model.addAttribute("foodOrders", java.util.Collections.emptyList());
        }
        return "guest/profile";
    }

    @GetMapping({"/update", "/edit"})
    public String redirectProfile() {
        return "redirect:/profile";
    }

    @PostMapping({"/edit", "/update"})
    public String editProfile(Authentication authentication, 
                              @RequestParam String fullName, 
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String cccd,
                              RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
        if (customer != null) {
            customer.setFullName(fullName);
            if (email != null && !email.trim().isEmpty()) {
                customer.setEmail(email.trim());
            }
            if (gender != null) {
                customer.setGender(gender);
            }
            if (phone != null) {
                customer.setPhone(phone);
            }
            if (cccd != null && !cccd.trim().isEmpty() && !"********".equals(cccd)) {
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
        String username = authentication.getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        
        if (account != null) {
            if (passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
                account.setPasswordHash(passwordEncoder.encode(newPassword));
                accountRepository.save(account);
                redirectAttributes.addFlashAttribute("success", "Äá»•i máº­t kháº©u thÃ nh cÃ´ng!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Máº­t kháº©u cÅ© khÃ´ng chÃ­nh xÃ¡c!");
            }
        }
        return "redirect:/profile";
    }
}
