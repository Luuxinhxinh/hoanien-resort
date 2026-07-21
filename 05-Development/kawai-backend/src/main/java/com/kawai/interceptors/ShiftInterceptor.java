package com.kawai.interceptors;

import com.kawai.services.interfaces.ShiftService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ShiftInterceptor implements HandlerInterceptor {

    @Autowired
    private ShiftService shiftService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Bỏ qua các API, static resources, login, error, out-of-shift
        if (uri.startsWith("/api/") || uri.startsWith("/css/") || uri.startsWith("/js/") 
            || uri.startsWith("/images/") || uri.startsWith("/fonts/") || uri.startsWith("/vendor/")
            || uri.equals("/login") || uri.equals("/logout") || uri.equals("/error") 
            || uri.equals("/out-of-shift") || uri.equals("/")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return true; // Để Security filter lo việc authentication
        }

        // Kiểm tra xem user có đang trong ca không
        boolean isOnShift = shiftService.checkIsOnShift(auth, request.getSession());
        
        if (!isOnShift) {
            // Nếu là request AJAX/Fetch, trả về 403 (dành cho các endpoint web dùng AJAX)
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith) || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"OUT_OF_SHIFT\", \"message\":\"Bạn đang ngoài ca làm việc.\"}");
                return false;
            }
            
            // Nếu là request bình thường (trình duyệt), redirect sang trang báo lỗi
            response.sendRedirect("/out-of-shift");
            return false;
        }

        return true;
    }
}
