// src/main/java/com/polarisoffice/security/support/PasswordGenerator.java
package com.polarisoffice.security.support;

import java.security.SecureRandom;

public final class PasswordGenerator {
    private static final String CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*";
    private static final SecureRandom RND = new SecureRandom();

    public static String generate(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i=0; i<len; i++) sb.append(CHARS.charAt(RND.nextInt(CHARS.length())));
        return sb.toString();
    }

    private PasswordGenerator() {}
}
