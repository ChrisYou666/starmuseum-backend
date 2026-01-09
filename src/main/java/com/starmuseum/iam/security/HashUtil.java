package com.starmuseum.iam.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Hash 工具：
 * - refresh token 绝对不能存明文（数据库/redis 都不存）
 * - 我们只存 SHA-256(pepper + refreshToken)
 */
public class HashUtil {

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
