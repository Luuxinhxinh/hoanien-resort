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

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
        model.addAttribute("customer", customer);
        return "guest/profile";
    }

    @PostMapping("/edit")
    public String editProfile(Authentication authentication, 
                              @RequestParam String fullName, 
                              @RequestParam String email, 
                              RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
        if (customer != null) {
            customer.setFullName(fullName);
            customer.setEmail(email);
            customerRepository.save(customer);
            redirectAttributes.addFlashAttribute("success", "Cáº­p nháº­t thÃ´ng tin thÃ nh cÃ´ng!");
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
