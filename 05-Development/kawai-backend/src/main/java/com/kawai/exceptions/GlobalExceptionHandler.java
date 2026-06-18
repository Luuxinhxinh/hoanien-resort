package com.kawai.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object handleError(HttpServletRequest request, Exception ex) {
        log.error("Lỗi khi truy cập {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // If it's an API request, return JSON instead of HTML
        if (request.getRequestURI().startsWith("/api/") || request.getRequestURI().startsWith("/admin/api/")) {
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", ex.getMessage(), "type", ex.getClass().getSimpleName()));
        }

        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorType", ex.getClass().getSimpleName());
        mav.addObject("requestUri", request.getRequestURI());
        mav.addObject("stackTrace", getStackTraceAsString(ex));
        mav.setViewName("error/custom-error");
        return mav;
    }

    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            if (element.getClassName().startsWith("com.kawai")) {
                sb.append(element.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}