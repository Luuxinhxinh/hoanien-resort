package com.kawai.config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.kawai.models.Account;
import com.kawai.services.impl.CustomOAuth2UserService;
import com.kawai.services.impl.OAuthAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private com.kawai.repositories.AuthorizedDeviceRepository authorizedDeviceRepository;

    @Autowired
    private com.kawai.repositories.AccountRepository accountRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuthAccountService oAuthAccountService;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ops-login", "/admin-backdoor").permitAll()
                        // .access(new
                        // org.springframework.security.web.access.expression.WebExpressionAuthorizationManager(
                        // "hasIpAddress('192.168.1.0/24')"))

                        .requestMatchers("/", "/booking", "/auth/register", "/auth/login", "/auth/check-session",
                                "/auth/google-login", "/auth/forgot-password", "/auth/reset-password", "/auth/manual-logout",
                                "/api/v1/auth/**",
                                "/h2-console/**", "/css/**", "/js/**", "/guest/**", "/uploads/**", "/api/v1/upload",
                                "/living", "/wellbeing", "/dining",
                                "/experiences", "/tours", "/tours/**", "/profile", "/order-food", "/AnhTour/**",
                                "/fbStaff/**", "/f&bStaff/**", "/kitchenStaff/**", "/api/menu-items/**", "/api/rooms/**",
                                "/api/v1/tables/**", "/api/pos/**",
                                "/api/bookings", "/api/bookings/**",
                                "/api/tour-bookings", "/api/tour-bookings/**", "/api/faceid/**", "/error",
                                "/api/v1/payments/vnpay-return", "/api/v1/payments/vnpay-ipn",
                                "/api/v1/payments/food-order/**", "/book-table", "/receptionist/remote-scan", "/api/v1/remote-scan/**")
                        .permitAll()

                        .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "OP_MASTER_DATA", "OP_AUDIT_LOG", "OP_WORKFLOW")
                        .requestMatchers("/manager/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        // Receptionist root & dashboard
                        .requestMatchers("/receptionist/dashboard").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_DASHBOARD",
                                "OP_RECEPTION_WALKIN", "OP_RECEPTION_CHECKIN", "OP_RECEPTION_CHECKOUT", "OP_RECEPTION_INHOUSE")
                        // Walk-in
                        .requestMatchers("/receptionist/walk-in", "/receptionist/walk-in/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_RECEPTION_WALKIN")
                        // Check-in
                        .requestMatchers("/receptionist/check-in", "/receptionist/check-in/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_RECEPTION_CHECKIN")
                        // Folio / Check-out
                        .requestMatchers("/receptionist/folio", "/receptionist/folio/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_RECEPTION_CHECKOUT")
                        // In-house
                        .requestMatchers("/receptionist/in-house", "/receptionist/in-house/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_RECEPTION_INHOUSE")
                        // Night-audit
                        .requestMatchers("/receptionist/night-audit", "/receptionist/night-audit/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_NIGHT_AUDIT")
                        // Housekeeping ops (via receptionist portal)
                        .requestMatchers("/receptionist/operations", "/receptionist/operations/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER", "OP_HOUSEKEEPING")
                        // Catch-all for any other /receptionist/** paths
                        .requestMatchers("/receptionist/**").hasAnyAuthority(
                                "ROLE_ADMIN", "ROLE_MANAGER",
                                "OP_RECEPTION_WALKIN", "OP_RECEPTION_CHECKIN", "OP_RECEPTION_CHECKOUT", "OP_RECEPTION_INHOUSE")
                        .requestMatchers("/staff/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers("/housekeeping/**").hasAnyAuthority("ROLE_HOUSEKEEPING", "ROLE_ADMIN", "OP_HOUSEKEEPING")
                        .requestMatchers("/maintenance/**").hasAnyAuthority("ROLE_MAINTENANCE", "ROLE_MAINTAINER", "ROLE_ADMIN", "OP_MAINTENANCE")
                        .requestMatchers("/tourguide/**").hasAnyAuthority("ROLE_TOURGUIDE", "ROLE_ADMIN", "OP_TOUR")
                        // F&B: sub-permissions govern each operation type
                        .requestMatchers("/fbStaff/**", "/f&bStaff/**").hasAnyAuthority(
                                "ROLE_FB_STAFF", "ROLE_ADMIN", "ROLE_MANAGER", "OP_FNB", "OP_FNB_ORDER", "OP_FNB_TABLE", "OP_FNB_ROOM_SERVICE")
                        .requestMatchers("/kitchenStaff/**").hasAnyAuthority(
                                "ROLE_FB_STAFF", "ROLE_ADMIN", "ROLE_MANAGER", "OP_FNB", "OP_FNB_ORDER")
                        .requestMatchers("/profile/**").authenticated()
                        .anyRequest().authenticated())

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                .formLogin(form -> form
                        .loginPage("/ops-login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(roleBasedSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/booking")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler))

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll());

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public org.springframework.security.web.authentication.AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String referer = request.getHeader("Referer");
            String errorType = "invalid";
            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                errorType = "disabled";
            } else if (exception instanceof org.springframework.security.authentication.LockedException) {
                errorType = "locked";
            }
            if (referer != null && !referer.trim().isEmpty() && !referer.contains("/ops-login")) {
                if (referer.contains("?")) {
                    response.sendRedirect(referer + "&login_error=true&error_type=" + errorType);
                } else {
                    response.sendRedirect(referer + "?login_error=true&error_type=" + errorType);
                }
            } else {
                response.sendRedirect("/ops-login?error=true&error_type=" + errorType);
            }
        };
    }

    @Bean
    public org.springframework.security.web.authentication.logout.LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.trim().isEmpty()) {
                if (referer.contains("/admin") || referer.contains("/manager") || referer.contains("/receptionist") || referer.contains("/fbStaff") || referer.contains("/kitchenStaff") || referer.contains("/tourguide") || referer.contains("/ops-login")) {
                    response.sendRedirect("/ops-login");
                } else if (referer.contains("/payment") || referer.contains("/profile")) {
                    response.sendRedirect("/booking");
                } else {
                    response.sendRedirect(referer);
                }
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {

                boolean isOpsUser = false;
                for (var authz : authentication.getAuthorities()) {
                    String role = authz.getAuthority();
                    if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER") ||
                            role.equals("ROLE_RECEPTIONIST") || role.equals("ROLE_STAFF") ||
                            role.equals("ROLE_FB_STAFF") || role.equals("ROLE_TOURGUIDE") ||
                            role.equals("ROLE_HOUSEKEEPING") || role.equals("ROLE_MAINTENANCE") || role.equals("ROLE_MAINTAINER")) {
                        isOpsUser = true;
                    }
                }

                String deviceId = request.getParameter("device_id");
                boolean isFromOpsPortal = (deviceId != null);

                if (isOpsUser) {
                    if (!isFromOpsPortal) {
                        request.getSession().invalidate();
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        response.sendRedirect("/booking?login_error=true&error_type=invalid");
                        return;
                    }
                    
                    // Device Authorization Check
                    java.util.Optional<com.kawai.models.AuthorizedDevice> optDevice = authorizedDeviceRepository.findByDeviceCode(deviceId);
                    if (optDevice.isEmpty()) {
                        // Tạm thời auto-approve thiết bị mới trong quá trình dev
                        com.kawai.models.AuthorizedDevice newDevice = new com.kawai.models.AuthorizedDevice();
                        newDevice.setDeviceCode(deviceId);
                        newDevice.setIsApproved(true);
                        authorizedDeviceRepository.save(newDevice);
                    } else if (!optDevice.get().getIsApproved()) {
                        request.getSession().invalidate();
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        request.getSession().setAttribute("authError", "Thiết bị của bạn không được cấp phép hoặc đã bị khóa.");
                        response.sendRedirect("/ops-login?error=unauthorized_device");
                        return;
                    }

                    String redirectTo = request.getParameter("redirect_to");
                    if (redirectTo != null && !redirectTo.trim().isEmpty()) {
                        response.sendRedirect(redirectTo);
                        return;
                    }

                    String redirect = "/";
                    for (var authz : authentication.getAuthorities()) {
                        String role = authz.getAuthority();
                        if (role.equals("ROLE_ADMIN")) {
                            redirect = "/admin/dashboard";
                            break;
                        } else if (role.equals("ROLE_MANAGER")) {
                            redirect = "/manager/dashboard";
                            break;
                        } else if (role.equals("ROLE_RECEPTIONIST") || role.equals("ROLE_STAFF")) {
                            redirect = "/receptionist/dashboard";
                            break;
                        } else if (role.equals("ROLE_FB_STAFF")) {
                            Account acc = accountRepository.findByUsername(authentication.getName()).orElse(null);
                            if (acc != null && "F&B KITCHEN".equalsIgnoreCase(acc.getRole().getRoleName())) {
                                redirect = "/kitchenStaff/dashboard";
                            } else {
                                redirect = "/fbStaff/dashboard";
                            }
                            break;
                        } else if (role.equals("ROLE_TOURGUIDE")) {
                            redirect = "/tourguide/dashboard";
                            break;
                        } else if (role.equals("ROLE_HOUSEKEEPING")) {
                            redirect = "/housekeeping/dashboard";
                            break;
                        } else if (role.equals("ROLE_MAINTENANCE") || role.equals("ROLE_MAINTAINER")) {
                            redirect = "/maintenance/dashboard";
                            break;
                        }
                    }

                    if (redirect.equals("/")) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        response.sendRedirect("/ops-login?error=true");
                        return;
                    }

                    Account account = accountRepository.findByUsername(authentication.getName()).orElse(null);
                    if (account != null) {
                        request.getSession().setAttribute("user", account);
                    }
                    response.sendRedirect(redirect);
                } else {
                    // Normal Customer / Guest
                    if (isFromOpsPortal) {
                        // Guest tried to log in from Ops portal
                        request.getSession().invalidate();
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        response.sendRedirect("/ops-login?error=true");
                        return;
                    }

                    Account account = accountRepository.findByUsername(authentication.getName()).orElse(null);
                    if (account != null) {
                        request.getSession().setAttribute("user", account);
                    }

                    String redirectTo = request.getParameter("redirect_to");
                    if (redirectTo != null && !redirectTo.trim().isEmpty()) {
                        response.sendRedirect(redirectTo);
                        return;
                    }

                    // Redirect to /living for all customers
                    response.sendRedirect("/living");
                }
            }
        };
    }
}
