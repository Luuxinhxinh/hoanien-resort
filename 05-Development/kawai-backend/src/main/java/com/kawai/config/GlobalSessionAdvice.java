package com.kawai.config;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalSessionAdvice {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private com.kawai.repositories.EmployeeRepository employeeRepository;

    @ModelAttribute("staffName")
    public String getStaffName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            com.kawai.models.Employee employee = employeeRepository.findByAccountUsername(auth.getName()).orElse(null);
            if (employee != null) {
                return employee.getFullName();
            }
        }
        return null;
    }

    @ModelAttribute
    public void syncSessionUser(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // 1. Sync from Session to Spring Security if Security Context is empty/anonymous but Session has user
        Account sessionUser = (Account) session.getAttribute("user");
        if (sessionUser != null) {
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                try {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        userDetailsService.loadUserByUsername(sessionUser.getUsername());
                    
                    if (userDetails != null && userDetails.isEnabled()) {
                        org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth = 
                            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                            );
                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                        auth = newAuth; // Update local variable for step 2 check
                        System.out.println("🔄 GlobalSessionAdvice: Re-authenticated user " + sessionUser.getUsername() + " from session to SecurityContext");
                    }
                } catch (Exception e) {
                    // Ignore and let standard flow handle it
                    System.err.println("❌ GlobalSessionAdvice: Failed to re-authenticate from session: " + e.getMessage());
                }
            }
        }

        // 2. Sync from Spring Security to Session if Session is empty but Security Context is authenticated
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            if (sessionUser == null) {
                String username = auth.getName();
                if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) auth;
                    if (oauthToken.getPrincipal() != null) {
                        String emailAttr = oauthToken.getPrincipal().getAttribute("email");
                        if (emailAttr != null) {
                            username = emailAttr;
                        }
                    }
                }
                Account account = accountRepository.findByUsername(username).orElse(null);
                if (account != null) {
                    session.setAttribute("user", account);
                    System.out.println("🔄 GlobalSessionAdvice: Synced user " + username + " from SecurityContext to session");
                }
            }
        }
    }
}
