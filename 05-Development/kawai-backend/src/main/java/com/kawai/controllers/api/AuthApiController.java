package com.kawai.controllers.api;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.services.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerAjax(@RequestParam String username,
                                          @RequestParam String password,
                                          @RequestParam String email,
                                          @RequestParam String fullName) {
        Map<String, Object> response = new HashMap<>();
        try {
            authService.register(username, password, email, fullName, "Other", null);
            authService.generate2FaOtp(username);
            
            response.put("success", true);
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpAjax(@RequestParam String username,
                                           @RequestParam String otpCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = authService.verify2FaOtp(username, otpCode);
            if (success) {
                response.put("success", true);
                response.put("message", "Xác thực OTP thành công! Vui lòng đăng nhập.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mã OTP không hợp lệ hoặc đã hết hạn.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPasswordAjax(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            authService.requestPasswordReset(email);
            response.put("success", true);
            response.put("message", "Link khôi phục đã được gửi đến email của bạn.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra, vui lòng thử lại.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPasswordAjax(@RequestParam String token,
                                               @RequestParam String newPassword,
                                               @RequestParam String confirmPassword) {
        Map<String, Object> response = new HashMap<>();
        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "Mật khẩu xác nhận không khớp.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean success = authService.resetPassword(token, newPassword);
            if (success) {
                response.put("success", true);
                response.put("message", "Đổi mật khẩu thành công! Vui lòng đăng nhập.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Mã OTP không hợp lệ hoặc đã hết hạn.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
