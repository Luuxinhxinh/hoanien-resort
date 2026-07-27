package com.hoanien.controllers.api;

import com.hoanien.models.Account;
import com.hoanien.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffProfileApiController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody Map<String, String> payload) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        
        String newPassword = payload.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mật khẩu mới không được để trống"));
        }

        String username = authentication.getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy tài khoản"));
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công"));
    }
}
