package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingController {
    
    @GetMapping({"/", "/booking"})
    public String showBookingPage(java.security.Principal principal, org.springframework.ui.Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/booking"; 
    }
}
