package com.kawai.security;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String rawRole = account.getRole().getRoleName();
        String securityRole = rawRole;
        if (rawRole.equalsIgnoreCase("Admin")) {
            securityRole = "ROLE_ADMIN";
        } else if (rawRole.equalsIgnoreCase("Manager")) {
            securityRole = "ROLE_MANAGER";
        } else if (rawRole.equalsIgnoreCase("Receptionist")) {
            securityRole = "ROLE_RECEPTIONIST";
        } else if (rawRole.toUpperCase().startsWith("F&B") || rawRole.equalsIgnoreCase("ROLE_FNB_STAFF") || rawRole.equalsIgnoreCase("ROLE_FB_STAFF")) {
            securityRole = "ROLE_FB_STAFF";
        } else if (rawRole.equalsIgnoreCase("Housekeeping")) {
            securityRole = "ROLE_HOUSEKEEPING";
        } else if (rawRole.equalsIgnoreCase("Tourguide")) {
            securityRole = "ROLE_TOURGUIDE";
        } else if (rawRole.startsWith("Khách") || rawRole.equalsIgnoreCase("ROLE_GUEST") || rawRole.equalsIgnoreCase("ROLE_CUSTOMER")) {
            securityRole = "ROLE_GUEST";
        } else {
            if (!securityRole.startsWith("ROLE_")) {
                securityRole = "ROLE_" + securityRole.toUpperCase();
            }
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(securityRole);

        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getUsername())
                .password(account.getPasswordHash())
                .authorities(Collections.singletonList(authority))
                .disabled(!account.getIsActive())
                .build();
    }
}
