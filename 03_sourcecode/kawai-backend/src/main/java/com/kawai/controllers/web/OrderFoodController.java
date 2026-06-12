package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class OrderFoodController {

    @GetMapping("/order-food")
    public String showOrderFoodPage(Principal principal, Model model) {
        model.addAttribute("isLoggedIn", principal != null);
        return "guest/order-food";
    }
}
