package com.kawai.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OAuth2RedirectTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private OAuth2SuccessHandler oAuth2SuccessHandler;

        @Test
        public void testGoogleLoginEndpointSetsCookieAndRedirects() throws Exception {
                mockMvc.perform(get("/auth/google-login").param("from", "/order-food"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                                .andExpect(cookie().value("OAUTH2_REDIRECT_URI", "/order-food"))
                                .andExpect(cookie().path("OAUTH2_REDIRECT_URI", "/"));
        }

        @Test
        public void testOAuth2SuccessHandlerRedirectsToCookieTarget() throws Exception {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
                OAuth2User principal = new DefaultOAuth2User(
                                Collections.singleton(authority),
                                Map.of("email", "test@example.com", "name", "Test User"),
                                "name");
                OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                                principal,
                                Collections.singleton(authority),
                                "google");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                request.setCookies(new Cookie("OAUTH2_REDIRECT_URI", "/order-food"));

                oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

                assertEquals("/order-food", response.getRedirectedUrl());
        }

        @Test
        public void testOAuth2SuccessHandlerRedirectsToSessionTarget() throws Exception {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
                OAuth2User principal = new DefaultOAuth2User(
                                Collections.singleton(authority),
                                Map.of("email", "test@example.com", "name", "Test User"),
                                "name");
                OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                                principal,
                                Collections.singleton(authority),
                                "google");

                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("OAUTH2_REDIRECT_URI", "/order-food");
                request.setSession(session);

                oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

                assertEquals("/order-food", response.getRedirectedUrl());
        }
}
