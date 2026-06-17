package com.kawai.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OAuth2RedirectTest {

    @Autowired
    private MockMvc mockMvc;

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
        mockMvc.perform(get("/login/oauth2/code/google")
                        .cookie(new Cookie("OAUTH2_REDIRECT_URI", "/order-food"))
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("email", "test@example.com");
                                    attrs.put("name", "Test User");
                                })))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order-food"));
    }

    @Test
    public void testOAuth2SuccessHandlerRedirectsToSessionTarget() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("OAUTH2_REDIRECT_URI", "/order-food");

        mockMvc.perform(get("/login/oauth2/code/google")
                        .session(session)
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("email", "test@example.com");
                                    attrs.put("name", "Test User");
                                })))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order-food"));
    }
}
