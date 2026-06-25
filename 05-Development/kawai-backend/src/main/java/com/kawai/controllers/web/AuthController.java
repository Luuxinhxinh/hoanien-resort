package com.kawai.controllers.web;

import com.kawai.models.Account;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

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

    @PostMapping("/manual-logout")
    public String manualLogout(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/ops-login";
    }
}