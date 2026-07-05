package com.kawai.controllers.web;

import com.kawai.models.Customer;
import com.kawai.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

/**
 * Trang thanh toán — UC10: Hoàn tất đặt phòng & thanh toán.
 * Nhận bookingId qua query param, load thông tin khách và chi tiết booking từ
 * API.
 */
@Controller
public class PaymentController {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * GET /payment?bookingId={id}
     * Hiển thị trang thanh toán đặt phòng.
     * bookingId được truyền từ booking.js sau khi tạo HOLD booking thành công.
     */
    @GetMapping("/payment")
    public String showPaymentPage(
            @RequestParam(required = false) Long bookingId,
            Principal principal,
            Authentication authentication,
            Model model) {

        boolean isLoggedIn = com.kawai.utils.SecurityUtils.isCustomerLoggedIn(principal);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("bookingId", bookingId);

        // Pre-fill customer info nếu đã đăng nhập
        if (isLoggedIn && authentication != null) {
            String username = authentication.getName();
            if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
                username = oauthToken.getPrincipal().getAttribute("email");
            }
            
            Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
            if (customer == null) {
                customer = customerRepository.findByEmail(username).orElse(null);
            }
            model.addAttribute("customer", customer);
        }

        return "guest/payment";
    }
}
