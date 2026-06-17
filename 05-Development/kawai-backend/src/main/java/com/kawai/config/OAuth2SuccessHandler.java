package com.kawai.config;

import com.kawai.models.Account;
import com.kawai.services.impl.OAuthAccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthAccountService oAuthAccountService;

    public OAuth2SuccessHandler(OAuthAccountService oAuthAccountService) {
        this.oAuthAccountService = oAuthAccountService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String fullName = oauthUser.getAttribute("name");

        if (email != null) {
            Account account = oAuthAccountService.findOrCreateOAuthAccount(email, fullName);
            if (account != null) {
                request.getSession().setAttribute("user", account);
            }
        }

        String savedRequest = null;
        Cookie[] cookies = request.getCookies();
        System.out.println(">>> OAuth2 Success Handler triggered. All cookies:");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("  Cookie: " + cookie.getName() + " = " + cookie.getValue());
                if ("OAUTH2_REDIRECT_URI".equals(cookie.getName())) {
                    savedRequest = cookie.getValue();
                    System.out.println("  Found OAUTH2_REDIRECT_URI cookie: " + savedRequest);
                    // Clear the cookie
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

        if (savedRequest == null) {
            HttpSession session = request.getSession(false);
            savedRequest = (session != null) ? (String) session.getAttribute("OAUTH2_REDIRECT_URI") : null;
            System.out.println("  Session check for OAUTH2_REDIRECT_URI: " + savedRequest);
            if (savedRequest != null && session != null) {
                session.removeAttribute("OAUTH2_REDIRECT_URI");
            }
        }

        System.out.println("  Final savedRequest decided: " + savedRequest);

        if (savedRequest != null && !savedRequest.trim().isEmpty()) {
            System.out.println("  Redirecting to savedRequest: " + savedRequest);
            response.sendRedirect(savedRequest);
        } else {
            String referer = request.getHeader("Referer");
            System.out.println("  Referer header: " + referer);
            if (referer != null && !referer.trim().isEmpty()
                    && !referer.contains("/oauth2/")
                    && !referer.contains("/login")) {
                System.out.println("  Redirecting to Referer: " + referer);
                response.sendRedirect(referer);
            } else {
                System.out.println("  Redirecting to default /booking");
                response.sendRedirect("/booking");
            }
        }
    }
}
