package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.kawai.repositories.CustomerRepository;
import java.util.Optional;
import com.kawai.models.Customer;
import java.security.Principal;

@Controller
public class BookingController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/")
    public String showHomePage() {
        return "redirect:/living";
    }

    @GetMapping("/booking")
    public String showBookingPage(Principal principal, Model model,
            @RequestParam(name = "keyword", required = false) String keyword) {
        model.addAttribute("isLoggedIn", principal != null);

        if (principal != null) {
            Optional<Customer> customerOpt = customerRepository
                    .findByAccount_Username(principal.getName());
            customerOpt.ifPresent(customer -> {
                model.addAttribute("customerName", customer.getFullName());
                model.addAttribute("customerEmail", customer.getEmail());
                model.addAttribute("customerPhone", customer.getPhone());
            });
        }

        model.addAttribute("keyword", keyword);

        return "guest/booking";
    }

    @GetMapping("/living")
    public String showLivingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/living";
    }

    @GetMapping("/wellbeing")
    public String showWellbeingPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/wellbeing";
    }

    @GetMapping("/dining")
    public String showDiningPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/dining";
    }

    @GetMapping("/experiences")
    public String showExperiencesPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/experiences";
    }

}
