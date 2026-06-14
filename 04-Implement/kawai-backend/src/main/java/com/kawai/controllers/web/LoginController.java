package com.kawai.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
public class LoginController {

    /**
     * Ops login page — unified entry for admin / manager / staff / receptionist.
     */
    @GetMapping("/ops-login")
    public String opsLogin() {
        return "ops-login";
    }

    /**
     * Handle the GET /logout links in the ops dashboard.
     */
    @GetMapping("/logout")
    public String customLogout(HttpServletRequest request, HttpServletResponse response) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler("JSESSIONID", "remember-me").logout(request, response, auth);
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/ops-login?logout=true";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.kawai.repositories.AuthorizedDeviceRepository authorizedDeviceRepository;

    @org.springframework.web.bind.annotation.PostMapping("/auth/login")
    public String handleLogin(@org.springframework.web.bind.annotation.RequestParam(value = "login_type", required = false) String loginType,
                              @org.springframework.web.bind.annotation.RequestParam(value = "device_id", required = false) String deviceId) {
        
        if ("ops".equals(loginType)) {
            if (deviceId == null || deviceId.isEmpty() || !authorizedDeviceRepository.existsByDeviceCodeAndIsApprovedTrue(deviceId)) {
                return "redirect:/ops-login?device_error=true";
            }
        }
        return "forward:/auth/process-login";
    }
}