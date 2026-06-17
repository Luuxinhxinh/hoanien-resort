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

@Configuration
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
                        .requestMatchers("/ops-login")
                        .access(new org.springframework.security.web.access.expression.WebExpressionAuthorizationManager(
                                "hasIpAddress('192.168.1.0/24')"))

                        .requestMatchers("/", "/booking", "/auth/register", "/auth/login", "/auth/check-session",
                                "/auth/google-login",
                                "/h2-console/**", "/css/**", "/js/**", "/guest/**", "/living", "/wellbeing", "/dining",
                                "/experiences", "/tours", "/tours/**", "/profile", "/order-food", "/AnhTour/**",
                                "/fbStaff/**", "/f&bStaff/**", "/api/menu-items/**", "/api/rooms/**", "/api/pos/**",
                                "/api/bookings", "/api/bookings/**",
                                "/api/tour-bookings", "/api/tour-bookings/**", "/api/faceid/**", "/error",
                                "/api/v1/payments/vnpay-return", "/api/v1/payments/vnpay-ipn")
                        .permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/tourguide/**").hasRole("TOURGUIDE")
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
            if (referer != null && !referer.trim().isEmpty() && !referer.contains("/ops-login")) {
                if (referer.contains("?")) {
                    response.sendRedirect(referer + "&login_error=true");
                } else {
                    response.sendRedirect(referer + "?login_error=true");
                }
            } else {
                response.sendRedirect("/ops-login?error=true");
            }
        };
    }

    @Bean
    public org.springframework.security.web.authentication.logout.LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.trim().isEmpty()) {
                response.sendRedirect(referer);
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
                            role.equals("ROLE_FB_STAFF") || role.equals("ROLE_TOURGUIDE")) {
                        isOpsUser = true;
                        break;
                    }
                }

                if (isOpsUser) {
                    String deviceId = request.getParameter("device_id");
                    if (deviceId == null || deviceId.trim().isEmpty()
                            || !authorizedDeviceRepository.existsByDeviceCodeAndIsApprovedTrue(deviceId)) {
                        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
                                .logout(request, response, authentication);
                        response.sendRedirect("/ops-login?device_error=true");
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
                            redirect = "/fbStaff/dashboard";
                            break;
                        } else if (role.equals("ROLE_TOURGUIDE")) {
                            redirect = "/tourguide/dashboard";
                            break;
                        }
                    }
                    response.sendRedirect(redirect);
                } else {
                    // Normal Customer / Guest
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