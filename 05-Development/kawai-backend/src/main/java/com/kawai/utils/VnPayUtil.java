package com.kawai.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class VnPayUtil {

    public static String hmacSHA512(final String key, final String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            hmac512.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("HMAC-SHA512 generation failed", ex);
        }
    }

    public static String buildRawHashString(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_")
                        && !e.getKey().equalsIgnoreCase("vnp_SecureHash")
                        && !e.getKey().equalsIgnoreCase("vnp_SecureHashType")
                        && e.getValue() != null && !e.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    try {
                        return e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.US_ASCII.toString());
                    } catch (java.io.UnsupportedEncodingException ex) {
                        return e.getKey() + "=" + e.getValue();
                    }
                })
                .collect(Collectors.joining("&"));
    }

    public static boolean validateSignature(Map<String, String> params, String receivedHash, String secret) {
        if (receivedHash == null || receivedHash.isEmpty()) return false;
        String rawHash = buildRawHashString(params);
        String computedHash = hmacSHA512(secret, rawHash);
        return computedHash.equalsIgnoreCase(receivedHash);
    }
}
