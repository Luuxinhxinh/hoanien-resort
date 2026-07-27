package com.hoanien.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private OAuthAccountService oAuthAccountService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");
        if (fullName == null) {
            fullName = "Khách hàng Google";
        }

        if (email != null) {
            // Gọi sang service tách riêng để đảm bảo @Transactional hoạt động đúng
            com.hoanien.models.Account account = oAuthAccountService.findOrCreateOAuthAccount(email, fullName);

            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_GUEST"));

            if (account != null && account.getRole() != null && account.getRole().getRoleName() != null) {
                String roleName = account.getRole().getRoleName().toUpperCase().replace(" ", "_");
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(roleName));
            }

            // Tạo attributes map mới với "email" làm key chính
            java.util.Map<String, Object> attrs = new java.util.HashMap<>(oAuth2User.getAttributes());
            attrs.put("email", email);

            // Trả về DefaultOAuth2User với authorities mới và nameAttributeKey = "email"
            // để principal.getName() trả về email thay vì Google ID
            return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                    authorities,
                    attrs,
                    "email");
        } else {
            log.warn("Google OAuth trả về email=null, bỏ qua việc tạo account");
            return oAuth2User;
        }
    }
}
