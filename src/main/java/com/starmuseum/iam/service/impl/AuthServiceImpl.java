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
 * 认证服务实现类（AuthServiceImpl）
 *
 * 这个类负责“账号认证相关”的核心流程（典型企业登录体系的最小闭环）：
 *
 * 1) register：注册
 *    - 写入 user（账号基本信息）
 *    - 初始化 user_privacy_setting（隐私设置一对一）
 *
 * 2) login：登录
 *    - 校验 email + password
 *    - 创建 refresh session（存 DB：user_session）
 *    - 生成 refresh token（返回给客户端）
 *    - refresh token 不存明文，存 hash（DB + Redis）
 *    - 生成 access token（JWT，短期有效，返回给客户端）
 *
 * 3) refresh：刷新 access token
 *    - 客户端传 refresh token（结构：{sessionId}.{random}）
 *    - 计算 refresh hash（pepper + refreshToken）
 *    - 优先走 Redis 校验（主路径，性能好）
 *    - Redis 未命中则回源 DB 校验（兜底路径，可靠性）
 *    - rotate：撤销旧 session，创建新 session，生成新 refresh token（更安全，防重放）
 *    - 签发新的 access token
 *
 * 4) logout：退出登录
 *    - 通过 refresh token 解析 sessionId
 *    - 撤销 session（DB 标记 revokedAt）并删除 Redis 缓存
 *
 * 关键设计思想：
 * - DB 是最终事实来源（revoked/expires 等都以 DB 为准）
 * - Redis 是高频校验加速（提高 refresh 性能、用 TTL 管理生命周期）
 * - refresh token 存 hash 而不是明文（防 DB 泄露导致 token 直接可用）
 * - refresh rotate（撤销旧、签发新）可以降低 refresh token 被重放的风险
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Redis Key 设计：
     * - starmuseum:refresh:sid:{sessionId}  -> refreshHash（用于快速校验 refresh token 是否匹配）
     * - starmuseum:refresh:uid:{sessionId}  -> userId（用于拿到会话对应用户，避免回源 DB）
     *
     * 说明：
     * 这里用 sessionId 作为 key 的一部分，原因是 refresh token 结构里包含 sessionId，
     * refresh 时可以直接定位到 Redis key，不需要额外索引。
     */
    private static final String REDIS_REFRESH_HASH_KEY_PREFIX = "starmuseum:refresh:sid:";
    private static final String REDIS_REFRESH_UID_KEY_PREFIX  = "starmuseum:refresh:uid:";

    // -------------------- 数据访问层（DB） --------------------

    /** 用户表访问：查询用户、创建用户 */
    private final UserMapper userMapper;

    /** 用户隐私设置表访问：注册时初始化一对一设置 */
    private final UserPrivacySettingMapper privacyMapper;

    /** refresh 会话表访问：创建 session、撤销 session、回源校验 */
    private final UserSessionMapper sessionMapper;

    // -------------------- 安全/Token 相关依赖 --------------------

    /**
     * 密码编码器：通常是 BCryptPasswordEncoder
     * 用于：
     * - 注册时 encode 明文密码 -> passwordHash
     * - 登录时 matches 校验密码
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT 工具：生成 access token（短期有效）
     * access token 一般只包含 userId 等必要 claim，不进 DB。
     */
    private final JwtUtil jwtUtil;

    /**
     * 安全配置：读取 refresh token 的 TTL、pepper 等配置
     * - ttlDays：refresh token 有效期
     * - pepper：服务端秘密字符串，用于 hash（防彩虹表/泄露后的直接比对）
     */
    private final SecurityProperties props;

    /**
     * Redis 操作：缓存 refreshHash + userId，提高 refresh 校验性能
     */
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

    // =========================================================
    // 1) 注册
    // =========================================================

    /**
     * 注册流程：
     * 1) 校验邮箱是否已注册
     * 2) 写入 user（保存密码 hash）
     * 3) 初始化隐私设置 user_privacy_setting（一对一）
     *
     * @param req 注册请求：包含 email/nickname/password
     */
    @Override
    @Transactional
    public void register(RegisterRequest req) {

        // 1) 邮箱是否已存在（防重复注册）
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getEmail, req.getEmail()));
        if (cnt != null && cnt > 0) {
            // 注意：这里抛 IllegalArgumentException，若全局异常没映射，可能会被当成 500
            throw new IllegalArgumentException("Email already registered");
        }

        // 2) 写入 user：密码必须保存 hash（绝不能保存明文）
        User u = new User();
        u.setEmail(req.getEmail());
        u.setNickname(req.getNickname());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword())); // BCrypt hash
        u.setStatus("NORMAL");
        userMapper.insert(u);

        // 3) 初始化隐私设置（一对一）
        //    这里给出默认策略：默认隐藏位置、公开内容等
        UserPrivacySetting ps = new UserPrivacySetting();
        ps.setUserId(u.getId());
        ps.setPostVisibilityDefault("PUBLIC");
        ps.setDefaultLocationVisibility(com.starmuseum.common.enums.LocationVisibility.HIDDEN.name());
        ps.setExactLocationPublicStrategy(com.starmuseum.common.enums.ExactLocationPublicStrategy.FUZZY.name());
        ps.setCreatedAt(LocalDateTime.now());
        ps.setUpdatedAt(LocalDateTime.now());
        privacyMapper.insert(ps);
    }

    // =========================================================
    // 2) 登录
    // =========================================================

    /**
     * 登录流程：
     * 1) 根据 email 找用户
     * 2) 校验密码
     * 3) 创建 user_session（先 insert 获取自增 sessionId）
     * 4) 生成 refresh token（格式：{sessionId}.{random}）
     * 5) refresh token 不存明文，只存 hash（DB）
     * 6) 同时把 hash + userId 缓存到 Redis（refresh 主路径走 Redis）
     * 7) 生成 access token（JWT）
     *
     * @param req 登录请求：email/password
     * @return accessToken + refreshToken
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {

        // 1) 根据 email 查用户
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getEmail, req.getEmail()));
        if (u == null) {
            // 不暴露“邮箱是否存在”，统一返回账号/密码错误（避免撞库枚举）
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 2) 校验密码（BCrypt）
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 3) 创建 session（DB）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = now.plusDays(props.getRefresh().getTtlDays());

        // refresh token 结构：{sessionId}.{random}
        // 由于 sessionId 需要 DB 自增得到，所以先 insert 占位
        UserSession s = new UserSession();
        s.setUserId(u.getId());
        s.setExpiresAt(exp);
        s.setRevokedAt(null);
        s.setCreatedAt(now);
        s.setLastUsedAt(now);

        // 先占位，insert 后再回填 refreshTokenHash
        s.setRefreshTokenHash("PENDING");
        sessionMapper.insert(s); // insert 后 s.getId() 有值

        // 4) 生成 refresh token（带 sessionId）
        String refreshToken = s.getId() + "." + TokenGenerator.randomUrlSafe(32);

        // 5) 计算 refresh hash（pepper + token）
        // pepper 是服务端秘密，防止数据库泄露后别人直接用 hash 反推 token（更难）
        String refreshHash = HashUtil.sha256Hex(props.getRefresh().getPepper() + refreshToken);

        // 6) 更新 DB 的 refresh_hash（只存 hash，不存明文）
        s.setRefreshTokenHash(refreshHash);
        sessionMapper.updateById(s);

        // 7) 写入 Redis（高频校验用）
        // refresh 时优先走 Redis，不命中才回源 DB
        cacheSessionToRedis(s.getId(), u.getId(), refreshHash);

        // 8) 生成 access token（短期有效）
        String accessToken = jwtUtil.generateAccessToken(u.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    // =========================================================
    // 3) 刷新 access token（并 rotate refresh token）
    // =========================================================

    /**
     * refresh 流程：
     * 1) 解析 refresh token 得到 sessionId
     * 2) 计算 refresh hash（用于比对）
     * 3) 优先走 Redis 校验（命中：对比 hash + 取 userId）
     * 4) Redis 未命中：回源 DB 校验（存在/未撤销/未过期/hash 匹配）
     * 5) rotate：撤销旧 session（防重放）
     * 6) 创建新 session + 新 refresh token，并缓存到 Redis
     * 7) 签发新的 access token
     *
     * @param req refresh 请求：refreshToken
     * @return 新的 accessToken + 新的 refreshToken
     */
    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest req) {

        // refresh token 结构：{sessionId}.{random}
        ParsedRefresh pr = parseRefresh(req.getRefreshToken());

        // 1) 计算 hash（必须和 login 时一致：pepper + refreshToken）
        String refreshHash = HashUtil.sha256Hex(props.getRefresh().getPepper() + req.getRefreshToken());

        // 2) 先走 Redis：快速校验（主路径）
        String redisHashKey = REDIS_REFRESH_HASH_KEY_PREFIX + pr.sessionId;
        String redisUidKey  = REDIS_REFRESH_UID_KEY_PREFIX + pr.sessionId;

        String cachedHash = redis.opsForValue().get(redisHashKey);
        String cachedUid  = redis.opsForValue().get(redisUidKey);

        Long userId;

        if (cachedHash != null && cachedUid != null) {
            // Redis 命中：hash 必须一致，否则 token 非法
            if (!Objects.equals(cachedHash, refreshHash)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            userId = Long.valueOf(cachedUid);
        } else {
            // 3) Redis 未命中：回源 DB（兜底路径，可靠性）
            UserSession s = sessionMapper.selectById(pr.sessionId);
            if (s == null) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            // 已撤销则不可用
            if (s.getRevokedAt() != null) {
                throw new IllegalArgumentException("Refresh token revoked");
            }
            // 过期不可用
            if (s.getExpiresAt() == null || s.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Refresh token expired");
            }
            // hash 必须一致
            if (!Objects.equals(s.getRefreshTokenHash(), refreshHash)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            userId = s.getUserId();

            // 回填 Redis，提升后续 refresh 性能
            cacheSessionToRedis(pr.sessionId, userId, refreshHash);
        }

        // 4) rotate：撤销旧 session
        // 安全收益：旧 refresh token 被用过一次后立即作废，可降低重放风险
        revokeSession(pr.sessionId);

        // 5) 创建新 session（DB）
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

        // 生成新的 refresh token + hash，并写回 DB
        String newRefreshToken = newS.getId() + "." + TokenGenerator.randomUrlSafe(32);
        String newRefreshHash = HashUtil.sha256Hex(props.getRefresh().getPepper() + newRefreshToken);

        newS.setRefreshTokenHash(newRefreshHash);
        sessionMapper.updateById(newS);

        // 缓存新 session 到 Redis
        cacheSessionToRedis(newS.getId(), userId, newRefreshHash);

        // 6) 生成新的 access token
        String newAccess = jwtUtil.generateAccessToken(userId);

        return new AuthResponse(newAccess, newRefreshToken);
    }

    // =========================================================
    // 4) 退出登录
    // =========================================================

    /**
     * logout 流程：
     * - 客户端传 refresh token
     * - 解析 sessionId
     * - 撤销该 session（DB 标记 revokedAt），并清理 Redis 缓存
     *
     * 安全细节：
     * - 即使 refresh token 是假的/过期的，也不建议返回“失败”，
     *   统一返回成功可以避免攻击者通过接口响应差异探测 session 是否存在。
     */
    @Override
    @Transactional
    public void logout(LogoutRequest req) {
        ParsedRefresh pr = parseRefresh(req.getRefreshToken());

        // 撤销 session
        // （即使 token 是假的，按安全考虑也统一返回成功，不暴露信息）
        revokeSession(pr.sessionId);
    }

    // =================== 内部方法 ===================

    /**
     * 把 session 信息缓存到 Redis（refresh 校验主路径）
     *
     * 缓存内容：
     * - sid -> refreshHash：校验 refresh token 是否正确
     * - sid -> userId：拿到用户ID用于签发 access token
     *
     * TTL：
     * - 与 refresh token TTL 对齐，避免 Redis 过期策略与 DB 不一致
     */
    private void cacheSessionToRedis(Long sessionId, Long userId, String refreshHash) {
        Duration ttl = Duration.ofDays(props.getRefresh().getTtlDays());

        redis.opsForValue().set(REDIS_REFRESH_HASH_KEY_PREFIX + sessionId, refreshHash, ttl);
        redis.opsForValue().set(REDIS_REFRESH_UID_KEY_PREFIX + sessionId, String.valueOf(userId), ttl);
    }

    /**
     * 撤销 session：
     * 1) DB：写 revoked_at = now（标记不可用）
     * 2) Redis：删除相关缓存 key（避免继续命中旧 hash）
     *
     * 说明：
     * - 如果 session 不存在或已撤销，方法也不会报错（幂等）
     * - 幂等性对 logout/refresh rotate 非常重要
     */
    private void revokeSession(Long sessionId) {

        // DB 标记撤销（如果已撤销也没关系）
        UserSession s = sessionMapper.selectById(sessionId);
        if (s != null && s.getRevokedAt() == null) {
            s.setRevokedAt(LocalDateTime.now());
            sessionMapper.updateById(s);
        }

        // 删除 Redis 缓存（无论 DB 是否存在都可以删，保持幂等）
        redis.delete(REDIS_REFRESH_HASH_KEY_PREFIX + sessionId);
        redis.delete(REDIS_REFRESH_UID_KEY_PREFIX + sessionId);
    }

    /**
     * 解析 refresh token：格式必须为 "{sessionId}.{random}"
     *
     * 解析结果：
     * - sessionId：用于定位 user_session（DB）或 Redis key
     *
     * 如果格式错误，统一抛 IllegalArgumentException（表示 token 非法）
     */
    private ParsedRefresh parseRefresh(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // refresh token 必须包含一个 '.' 分隔为两段
        String[] parts = refreshToken.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 第一段必须是正整数 sessionId
        try {
            long sid = Long.parseLong(parts[0]);
            if (sid <= 0) throw new IllegalArgumentException("Invalid refresh token");
            return new ParsedRefresh(sid);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    /**
     * refresh token 解析结果的简单封装
     */
    private static class ParsedRefresh {
        private final Long sessionId;
        private ParsedRefresh(Long sessionId) { this.sessionId = sessionId; }
    }
}
