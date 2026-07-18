package com.kawai.utils;

public class ValidationUtils {

    /**
     * Validate Phone Number: Must start with '0' or '+84' and followed by valid VN
     * mobile prefix (3,5,7,8,9).
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.trim();
        return cleanPhone.matches("^(0|\\+84)[35789]\\d{8}$");
    }

    /**
     * Validate CCCD/Passport format.
     */

    public static boolean isValidCccd(String value) {
        return value.matches("^\\d{12}$");
    }

    public static boolean isValidPassport(String value) {
        return value.matches("^[A-Za-z0-9]{6,15}$");
    }

    public static boolean isValidDocument(String value) {
        return isValidCccd(value) || isValidPassport(value);
    }
}
