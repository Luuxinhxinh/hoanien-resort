package com.kawai.utils;

public class ValidationUtils {

    /**
     * Validate Phone Number: Must be exactly 10 digits and start with '0'.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.trim();
        return cleanPhone.matches("^0\\d{9}$");
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
