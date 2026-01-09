package com.starmuseum.iam.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token 生成器：
 * - refresh token 我们使用：{sessionId}.{random}
 * - random 部分用强随机数生成，再 base64url 编码（不含 +/=/）
 */
public class TokenGenerator {

    private static final SecureRandom RND = new SecureRandom();

    public static String randomUrlSafe(int byteLen) {
        byte[] bytes = new byte[byteLen];
        RND.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
