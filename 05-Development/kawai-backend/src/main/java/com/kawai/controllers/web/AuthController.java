package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;

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
            return "redirect:" + redirectPath;
        }

        if (customerRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("authError", "Email này đã được sử dụng!");
            return "redirect:" + redirectPath;
        }

        // Assign default role (CUSTOMER NORMAL or CUSTOMER)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER NORMAL")
                .or(() -> roleRepository.findByRoleName("CUSTOMER"))
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("CUSTOMER");
                    return roleRepository.save(newRole);
                });

        // Create Account
        Account account = new Account();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(customerRole);
        account = accountRepository.save(account);

        // Create Customer
        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(fullName != null && !fullName.trim().isEmpty() ? fullName : username);
        customer.setEmail(email);
        customer.setGender(gender);
        customer.setPhone(phone != null && !phone.trim().isEmpty() ? phone : "0000000000");
        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("authSuccess", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:" + redirectPath;
    }

    /**
     * Endpoint trung gian: lưu trang hiện tại vào session trước khi redirect sang Google OAuth.
     * Giúp OAuth2 success handler biết cần quay lại trang nào sau khi đăng nhập thành công.
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
            } catch (Exception ignored) {}
        }

        session.setAttribute("OAUTH2_REDIRECT_URI", redirectAfter);

        // Also save to a cookie to bypass Spring Security session fixation/invalidation issues
        jakarta.servlet.http.Cookie redirectCookie = new jakarta.servlet.http.Cookie("OAUTH2_REDIRECT_URI", redirectAfter);
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
}