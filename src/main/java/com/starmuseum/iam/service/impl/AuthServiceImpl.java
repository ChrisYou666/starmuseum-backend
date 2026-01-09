package com.starmuseum.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.iam.config.SecurityProperties;
import com.starmuseum.iam.dto.LoginRequest;
import com.starmuseum.iam.dto.LogoutRequest;
import com.starmuseum.iam.dto.RefreshRequest;
import com.starmuseum.iam.dto.RegisterRequest;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.entity.UserPrivacySetting;
import com.starmuseum.iam.entity.UserSession;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.iam.mapper.UserPrivacySettingMapper;
import com.starmuseum.iam.mapper.UserSessionMapper;
import com.starmuseum.iam.security.HashUtil;
import com.starmuseum.iam.security.JwtUtil;
import com.starmuseum.iam.security.TokenGenerator;
import com.starmuseum.iam.service.AuthService;
import com.starmuseum.iam.vo.AuthResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AuthService 实现：
 * - 注册写 user + privacy
 * - 登录：校验密码 -> 生成 session(存DB) -> refreshToken(返回) -> refreshHash 存 Redis+DB
 * - refresh：校验 Redis/DB 中的 refreshHash -> rotate（撤销旧 session，生成新 session）
 * - logout：撤销 session，并清理 redis
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String REDIS_REFRESH_HASH_KEY_PREFIX = "starmuseum:refresh:sid:";
    private static final String REDIS_REFRESH_UID_KEY_PREFIX  = "starmuseum:refresh:uid:";

    private final UserMapper userMapper;
    private final UserPrivacySettingMapper privacyMapper;
    private final UserSessionMapper sessionMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityProperties props;
    private final StringRedisTemplate redis;

    public AuthServiceImpl(UserMapper userMapper,
                           UserPrivacySettingMapper privacyMapper,
                           UserSessionMapper sessionMapper,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           SecurityProperties props,
                           StringRedisTemplate redis) {
        this.userMapper = userMapper;
        this.privacyMapper = privacyMapper;
        this.sessionMapper = sessionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.props = props;
        this.redis = redis;
    }

    @Override
    @Transactional
    public void register(RegisterRequest req) {
        // 1) 邮箱是否已存在
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getEmail, req.getEmail()));
        if (cnt != null && cnt > 0) {
            throw new IllegalArgumentException("Email already registered");
        }

        // 2) 写入 user
        User u = new User();
        u.setEmail(req.getEmail());
        u.setNickname(req.getNickname());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setStatus("NORMAL");
        userMapper.insert(u);

        // 3) 初始化隐私设置（一对一）
        UserPrivacySetting ps = new UserPrivacySetting();
        ps.setUserId(u.getId());
        ps.setPostVisibilityDefault("PUBLIC");
        privacyMapper.insert(ps);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        // 1) 根据 email 查用户
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getEmail, req.getEmail()));
        if (u == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 2) 校验密码（BCrypt）
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 3) 创建 session（DB）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = now.plusDays(props.getRefresh().getTtlDays());

        // refresh token 格式：{sessionId}.{random}，sessionId 先要有，所以先 insert 拿到自增ID
        UserSession s = new UserSession();
        s.setUserId(u.getId());
        s.setExpiresAt(exp);
        s.setRevokedAt(null);
        s.setCreatedAt(now);
        s.setLastUsedAt(now);

        // 先占位，insert 后再回填 refreshTokenHash
        s.setRefreshTokenHash("PENDING");
        sessionMapper.insert(s);

        // 4) 生成 refresh token（带 sessionId）
        String refreshToken = s.getId() + "." + TokenGenerator.randomUrlSafe(32);

        // 5) 计算 refresh hash（pepper + token）
        String refreshHash = HashUtil.sha256Hex(props.getRefresh().getPepper() + refreshToken);

        // 6) 更新 DB 的 refresh_hash
        s.setRefreshTokenHash(refreshHash);
        sessionMapper.updateById(s);

        // 7) 写入 Redis（高频校验用）
        cacheSessionToRedis(s.getId(), u.getId(), refreshHash);

        // 8) 生成 access token（短期有效）
        String accessToken = jwtUtil.generateAccessToken(u.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        // refresh token 结构：{sessionId}.{random}
        ParsedRefresh pr = parseRefresh(req.getRefreshToken());

        // 1) 计算 hash
        String refreshHash = HashUtil.sha256Hex(props.getRefresh().getPepper() + req.getRefreshToken());

        // 2) 先走 Redis：快速校验（主路径）
        String redisHashKey = REDIS_REFRESH_HASH_KEY_PREFIX + pr.sessionId;
        String redisUidKey  = REDIS_REFRESH_UID_KEY_PREFIX + pr.sessionId;

        String cachedHash = redis.opsForValue().get(redisHashKey);
        String cachedUid  = redis.opsForValue().get(redisUidKey);

        Long userId;
        if (cachedHash != null && cachedUid != null) {
            // Redis 命中，hash 必须一致
            if (!Objects.equals(cachedHash, refreshHash)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            userId = Long.valueOf(cachedUid);
        } else {
            // 3) Redis 未命中，回源 DB（兜底路径）
            UserSession s = sessionMapper.selectById(pr.sessionId);
            if (s == null) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            if (s.getRevokedAt() != null) {
                throw new IllegalArgumentException("Refresh token revoked");
            }
            if (s.getExpiresAt() == null || s.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Refresh token expired");
            }
            if (!Objects.equals(s.getRefreshTokenHash(), refreshHash)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            userId = s.getUserId();

            // 回填 Redis，提升后续 refresh 性能
            cacheSessionToRedis(pr.sessionId, userId, refreshHash);
        }

        // 4) rotate：撤销旧 session，生成新 session（更安全，可防重放）
        revokeSession(pr.sessionId);

        // 5) 创建新 session
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = now.plusDays(props.getRefresh().getTtlDays());

        UserSession newS = new UserSession();
        newS.setUserId(userId);
        newS.setExpiresAt(exp);
        newS.setRevokedAt(null);
        newS.setCreatedAt(now);
        newS.setLastUsedAt(now);
        newS.setRefreshTokenHash("PENDING");
        sessionMapper.insert(newS);

        String newRefreshToken = newS.getId() + "." + TokenGenerator.randomUrlSafe(32);
        String newRefreshHash = HashUtil.sha256Hex(props.getRefresh().getPepper() + newRefreshToken);

        newS.setRefreshTokenHash(newRefreshHash);
        sessionMapper.updateById(newS);

        cacheSessionToRedis(newS.getId(), userId, newRefreshHash);

        // 6) 生成新的 access token
        String newAccess = jwtUtil.generateAccessToken(userId);

        return new AuthResponse(newAccess, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest req) {
        ParsedRefresh pr = parseRefresh(req.getRefreshToken());
        // 撤销 session（即使 token 是假的，按安全考虑也统一返回成功，不暴露信息）
        revokeSession(pr.sessionId);
    }

    // =================== 内部方法 ===================

    /**
     * 把 session 信息缓存到 Redis（refresh 校验主路径）
     */
    private void cacheSessionToRedis(Long sessionId, Long userId, String refreshHash) {
        Duration ttl = Duration.ofDays(props.getRefresh().getTtlDays());

        redis.opsForValue().set(REDIS_REFRESH_HASH_KEY_PREFIX + sessionId, refreshHash, ttl);
        redis.opsForValue().set(REDIS_REFRESH_UID_KEY_PREFIX + sessionId, String.valueOf(userId), ttl);
    }

    /**
     * 撤销 session：DB 写 revoked_at + Redis 删除缓存
     */
    private void revokeSession(Long sessionId) {
        // DB 标记撤销（如果已撤销也没关系）
        UserSession s = sessionMapper.selectById(sessionId);
        if (s != null && s.getRevokedAt() == null) {
            s.setRevokedAt(LocalDateTime.now());
            sessionMapper.updateById(s);
        }

        // 删除 Redis 缓存
        redis.delete(REDIS_REFRESH_HASH_KEY_PREFIX + sessionId);
        redis.delete(REDIS_REFRESH_UID_KEY_PREFIX + sessionId);
    }

    /**
     * 解析 refresh token：{sessionId}.{random}
     */
    private ParsedRefresh parseRefresh(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String[] parts = refreshToken.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        try {
            long sid = Long.parseLong(parts[0]);
            if (sid <= 0) throw new IllegalArgumentException("Invalid refresh token");
            return new ParsedRefresh(sid);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    private static class ParsedRefresh {
        private final Long sessionId;
        private ParsedRefresh(Long sessionId) { this.sessionId = sessionId; }
    }
}
