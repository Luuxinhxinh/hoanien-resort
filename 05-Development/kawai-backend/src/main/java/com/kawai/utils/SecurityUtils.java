package com.kawai.utils;

import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Checks if the currently authenticated user is a normal customer (not an ops/staff user).
     * This is used to ensure that staff sessions do not leak into the customer-facing interface.
     */
    public static boolean isCustomerLoggedIn(Principal principal) {
        if (principal == null) {
            return false;
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }
        
        // Check if any authority matches ops roles
        return auth.getAuthorities().stream().noneMatch(a -> {
            String role = a.getAuthority();
            return role.equals("ROLE_ADMIN") || 
                   role.equals("ROLE_MANAGER") || 
                   role.equals("ROLE_RECEPTIONIST") || 
                   role.equals("ROLE_STAFF") || 
                   role.equals("ROLE_FB_STAFF") || 
                   role.equals("ROLE_TOURGUIDE");
        });
    }
}
