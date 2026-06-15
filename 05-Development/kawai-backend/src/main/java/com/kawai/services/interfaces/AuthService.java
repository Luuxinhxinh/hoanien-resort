package com.kawai.services.interfaces;

public interface AuthService {
    boolean register(String username, String password, String email, String fullName, String gender, String phone);
    boolean login(String username, String password);
    String generate2FaOtp(String username);
    boolean verify2FaOtp(String username, String code);
    String requestPasswordReset(String email);
    boolean resetPassword(String token, String newPassword);
}
