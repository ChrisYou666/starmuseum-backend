package com.starmuseum.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Phase 4.5 - 请求日志标准化（简化链路追踪）
 *
 * 目标：
 * - 每个请求都有 requestId/traceId
 * - MDC 注入，方便日志统一携带
 * - 统一输出 method/path/status/costMs/requestId/traceId/userId(可脱敏)
 *
 * 约定：
 * - 入参支持 header: X-Request-Id / X-Trace-Id
 * - 输出回写 response header: X-Request-Id / X-Trace-Id
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_USER_ID = "userId";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        long startNs = System.nanoTime();

        String requestId = firstNonBlank(request.getHeader(HEADER_REQUEST_ID), genId());
        String traceId = firstNonBlank(request.getHeader(HEADER_TRACE_ID), requestId);

        // 这里先做“简化版 userId”：
        // - 没登录： "-"
        // - 已登录：尽量从 SecurityContext / 你项目的用户上下文拿
        // 你们项目有 JwtAuthenticationFilter，通常最终会把 userId 放进 Authentication principal 或 details
        String userId = tryGetUserId();
        String maskedUserId = maskUserId(userId);

        // 注入 MDC，后续任意日志都自动带上 requestId/traceId/userId
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_USER_ID, maskedUserId);

        // 回写到响应头（方便前端/网关/排查）
        response.setHeader(HEADER_REQUEST_ID, requestId);
        response.setHeader(HEADER_TRACE_ID, traceId);

        int status = 200;
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } catch (Exception e) {
            // 异常也要记录 status（很多时候还没写入 response，会是 200，这里保留）
            status = response.getStatus();
            throw e;
        } finally {
            long costMs = (System.nanoTime() - startNs) / 1_000_000;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();

            // 统一一条 INFO 请求日志（结构化 key=value 风格，后续可直接接 ELK）
            // 关键字段：method/path/status/costMs/requestId/traceId/userId
            if (query != null && !query.isBlank()) {
                log.info("req method={} path={} query={} status={} costMs={} requestId={} traceId={} userId={}",
                    method, uri, query, status, costMs, requestId, traceId, maskedUserId);
            } else {
                log.info("req method={} path={} status={} costMs={} requestId={} traceId={} userId={}",
                    method, uri, status, costMs, requestId, traceId, maskedUserId);
            }

            // 必须清理，避免线程复用导致串号
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_TRACE_ID);
            MDC.remove(MDC_USER_ID);
        }
    }

    /**
     * 生成短一些、可读性更好的 id
     */
    private String genId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        return b;
    }

    /**
     * TODO：你们可以后续把这里换成“真实 userId”（来自 JWT / Authentication）
     * 先保证 Phase 4.5 最短验收：日志里有 userId 字段（可脱敏）
     */
    private String tryGetUserId() {
        try {
            // 如果你们项目有统一的 AuthContext/CurrentUser 工具类，可以在这里读取
            // 例如：Long uid = AuthContext.getUserId(); return uid == null ? "-" : String.valueOf(uid);
            return "-";
        } catch (Exception ignore) {
            return "-";
        }
    }

    /**
     * 脱敏：只保留末尾 4 位（或保持 "-"）
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.isBlank() || "-".equals(userId)) return "-";
        String s = userId.trim();
        if (s.length() <= 4) return "****" + s;
        return "****" + s.substring(s.length() - 4);
    }
}
