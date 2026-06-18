package com.kawai.services.impl;

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
            oAuthAccountService.findOrCreateOAuthAccount(email, fullName);
            
            // Trả về DefaultOAuth2User với key là "email" để principal.getName() lấy được email thay vì Google ID
            return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                    oAuth2User.getAuthorities(),
                    oAuth2User.getAttributes(),
                    "email"
            );
        } else {
            log.warn("Google OAuth trả về email=null, bỏ qua việc tạo account");
            return oAuth2User;
        }
    }
}
