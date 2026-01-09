package com.starmuseum.iam.security;

import com.starmuseum.iam.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具类：
 * - accessToken 用 JWT（短期有效）
 * - refreshToken 不用 JWT（我们用随机串 + Redis/DB 会话管理，方便撤销）
 */
public class JwtUtil {

    private final SecurityProperties props;
    private final SecretKey key;

    public JwtUtil(SecurityProperties props) {
        this.props = props;
        // jjwt 0.12+ 推荐用 Keys.hmacShaKeyFor
        this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 accessToken
     * @param userId 用户ID
     */
    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds((long) props.getJwt().getAccessTokenTtlMinutes() * 60);

        return Jwts.builder()
            .issuer(props.getJwt().getIssuer())
            .subject(String.valueOf(userId)) // sub = userId
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key)
            .compact();
    }

    /**
     * 解析 accessToken，返回 claims
     */
    public Claims parseAccessToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
