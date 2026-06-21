package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;
import com.kawai.services.interfaces.AuthService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false, defaultValue = "Other") String gender,
            @RequestParam(required = false) String phone,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {

        String redirectPath = "/booking";
        if (referer != null && !referer.trim().isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                redirectPath = uri.getPath();
            } catch (Exception e) {
                redirectPath = "/booking";
            }
        }

        if (accountRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("authError", "Tên đăng nhập đã tồn tại!");
            redirectAttributes.addFlashAttribute("showRegister", true);
            return "redirect:" + redirectPath + "?register=true";
        }

        if (customerRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("authError", "Email này đã được sử dụng!");
            redirectAttributes.addFlashAttribute("showRegister", true);
            return "redirect:" + redirectPath + "?register=true";
        }

        // Assign default role (CUSTOMER NORMAL or CUSTOMER)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER NORMAL")
                .or(() -> roleRepository.findByRoleName("CUSTOMER"))
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("CUSTOMER");
                    return roleRepository.save(newRole);
                });

        // Create Account (chưa active cho đến khi verify OTP)
        Account account = new Account();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(customerRole);
        account.setIsActive(false);
        account = accountRepository.save(account);

        // Create Customer
        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(fullName != null && !fullName.trim().isEmpty() ? fullName : username);
        customer.setEmail(email);
        customer.setGender(gender);
        customer.setPhone(phone != null && !phone.trim().isEmpty() ? phone : "0000000000");
        customerRepository.save(customer);

        // Gửi OTP xác nhận đăng ký
        try {
            authService.generate2FaOtp(username);
            redirectAttributes.addFlashAttribute("authSuccess",
                    "Đăng ký thành công! Vui lòng kiểm tra email để xác thực OTP.");
            return "redirect:/auth/verify-otp?username=" + username;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("authError",
                    "Đăng ký thành công nhưng không thể gửi email xác thực. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("showRegister", true);
            return "redirect:" + redirectPath + "?register=true";
        }
    }

    /**
     * Endpoint trung gian: lưu trang hiện tại vào session trước khi redirect sang
     * Google OAuth.
     * Giúp OAuth2 success handler biết cần quay lại trang nào sau khi đăng nhập
     * thành công.
     */
    @GetMapping("/google-login")
    public String googleLogin(
            @RequestParam(value = "from", required = false) String from,
            @RequestHeader(value = "Referer", required = false) String referer,
            HttpSession session,
            jakarta.servlet.http.HttpServletResponse response) {

        String redirectAfter = "/booking"; // default
        if (from != null && !from.trim().isEmpty()) {
            redirectAfter = from;
        } else if (referer != null && !referer.trim().isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                redirectAfter = uri.getPath();
            } catch (Exception ignored) {
            }
        }

        session.setAttribute("OAUTH2_REDIRECT_URI", redirectAfter);

        // Also save to a cookie to bypass Spring Security session fixation/invalidation
        // issues
        jakarta.servlet.http.Cookie redirectCookie = new jakarta.servlet.http.Cookie("OAUTH2_REDIRECT_URI",
                redirectAfter);
        redirectCookie.setPath("/");
        redirectCookie.setMaxAge(300); // 5 minutes
        response.addCookie(redirectCookie);

        return "redirect:/oauth2/authorization/google";
    }

    // ── XÁC THỰC OTP ĐĂNG KÝ ──
    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam String username,
            RedirectAttributes redirectAttributes) {
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Thiếu thông tin người dùng.");
            return "redirect:/auth/login";
        }
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String username,
            @RequestParam String otpCode,
            RedirectAttributes redirectAttributes) {
        try {
            boolean isValid = authService.verify2FaOtp(username, otpCode);
            if (isValid) {
                redirectAttributes.addFlashAttribute("authSuccess",
                        "Xác thực OTP thành công! Tài khoản đã được kích hoạt. Vui lòng đăng nhập.");
                return "redirect:/booking?login=true";
            } else {
                redirectAttributes.addFlashAttribute("authError", "Mã OTP không hợp lệ.");
                return "redirect:/auth/verify-otp?username=" + username;
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("authError", e.getMessage());
            return "redirect:/auth/verify-otp?username=" + username;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("authError", "Có lỗi xảy ra. Vui lòng thử lại.");
            return "redirect:/auth/verify-otp?username=" + username;
        }
    }

    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam String username,
            RedirectAttributes redirectAttributes) {
        try {
            String otp = authService.generate2FaOtp(username);
            redirectAttributes.addFlashAttribute("authSuccess",
                    "Mã OTP mới đã được gửi đến email của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("authError", "Không thể gửi OTP. Vui lòng thử lại.");
        }
        return "redirect:/auth/verify-otp?username=" + username;
    }

    // ── QUÊN MẬT KHẨU ──
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            authService.requestPasswordReset(email);
            redirectAttributes.addFlashAttribute("authSuccess",
                    "Yêu cầu đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email.");
        } catch (Exception e) {
            // Không lộ thông tin email tồn tại hay không
            redirectAttributes.addFlashAttribute("authSuccess",
                    "Nếu email tồn tại trong hệ thống, link đặt lại mật khẩu đã được gửi.");
        }
        return "redirect:/auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token,
            RedirectAttributes redirectAttributes) {
        if (token == null || token.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Token không hợp lệ.");
            return "redirect:/booking?login=true";
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("authError", "Mật khẩu xác nhận không khớp.");
            return "redirect:/auth/reset-password?token=" + token;
        }
        try {
            boolean result = authService.resetPassword(token, newPassword);
            if (result) {
                redirectAttributes.addFlashAttribute("authSuccess",
                        "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
                return "redirect:/booking?login=true";
            } else {
                redirectAttributes.addFlashAttribute("authError", "Đặt lại mật khẩu thất bại.");
                return "redirect:/auth/reset-password?token=" + token;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("authError", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }

    // ── ENDPOINT KIỂM TRA SESSION ĐÃ ĐƯỢC CHUẨN HÓA ──
    @GetMapping("/check-session")
    @ResponseBody
    public String checkSession(HttpSession session) {
        // Lấy thuộc tính "user" từ Session
        Account user = (Account) session.getAttribute("user");

        if (user != null) {
            return "🎉 KIỂM TRA: Session ĐÃ TỒN TẠI! Username: " + user.getUsername()
                    + " | Quyền: " + (user.getRole() != null ? user.getRole().getRoleName() : "Chưa gán");
        } else {
            return "❌ KIỂM TRA: Session TRỐNG RỖNG (Bạn chưa đăng nhập hoặc chưa được nạp dữ liệu thành công)!";
        }
    }
}