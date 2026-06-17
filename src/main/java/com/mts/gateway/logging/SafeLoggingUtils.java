package com.mts.gateway.logging;

public class SafeLoggingUtils {
    
    private SafeLoggingUtils() {}

    public static String maskJwt(String token) {
        if (token == null || token.length() <= 12) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }
}
