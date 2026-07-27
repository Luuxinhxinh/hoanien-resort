package com.hoanien.utils;

public class ValidationUtils {

    public static String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String cleanPhone = phone.replaceAll("[\\s\\-\\.]", "");
        // If user typed 84 instead of +84 (and length is 11), add the +
        if (cleanPhone.startsWith("84") && cleanPhone.length() == 11) {
            cleanPhone = "+" + cleanPhone;
        }
        return cleanPhone;
    }

    /**
     * Validate Phone Number: Must start with '0' or '+84' and followed by valid VN
     * mobile prefix (3,5,7,8,9).
     */
    public static boolean isValidPhone(String phone) {
        String cleanPhone = normalizePhone(phone);
        if (cleanPhone.isEmpty()) {
            return false;
        }
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
