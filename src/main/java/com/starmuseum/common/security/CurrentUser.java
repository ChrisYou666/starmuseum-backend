package com.starmuseum.common.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从 Spring Security 的上下文中获取当前登录用户信息。
 *
 * 兼容策略：
 * - principal 是 Long/Integer：直接返回
 * - authentication.getName() 是数字字符串：解析成 Long
 *
 * DEV 兜底（用于你暂不处理 security 的阶段）：
 * - 若 SecurityContext 无法解析 userId，则尝试从请求头读取：
 *   X-User-Id / X-Dev-User-Id
 */
@Data
public class CurrentUser {

    private CurrentUser() {}

    public static Long requireUserId() {
        Long uid = getUserIdOrNull();
        if (uid == null) {
            throw new IllegalStateException("Unauthenticated (cannot resolve userId)");
        }
        return uid;
    }

    /**
     * 获取当前请求对应的用户 ID（可能为 null）。
     *
     * 设计目的：
     * 1) 优先从 Spring Security 的 SecurityContext 中获取当前认证用户（正式环境标准方式）。
     * 2) 如果 SecurityContext 没有（例如未接入认证/本地调试/单测），则在开发环境允许从请求头读取兜底用户 ID。
     *
     * 返回值说明：
     * - 返回 Long：成功解析到用户 ID
     * - 返回 null：无法获取/无法解析/未登录/无上下文
     *
     * 注意事项：
     * - 这是一个“容错型工具方法”，内部吞掉异常，避免影响主流程。
     * - 请求头兜底属于 DEV 方式，生产环境建议关闭或限制使用。
     * - 支持的请求头：X-User-Id / X-Dev-User-Id
     */
    public static Long getUserIdOrNull() {

        // -------------------------
        // 1) 正常路径：从 SecurityContext 取用户身份（生产环境标准）
        // -------------------------
        try {
            // 从线程上下文里获取当前请求绑定的 Authentication（由 Spring Security 注入）
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // auth != null：当前线程有安全上下文
            // auth.isAuthenticated()：当前用户已通过认证（注意 anonymous 情况可能也为 true，需结合实际配置判断）
            if (auth != null && auth.isAuthenticated()) {

                // principal 是“当前用户主体”，不同项目可能放 UserDetails / 自定义对象 / userId 等
                Object principal = auth.getPrincipal();

                // 情况 A：项目里把 userId 直接作为 principal（Long）
                if (principal instanceof Long l) {
                    return l;
                }

                // 情况 B：项目里把 userId 作为 Integer 放入 principal
                if (principal instanceof Integer i) {
                    return i.longValue();
                }

                // 情况 C：无法从 principal 直接拿到数字 ID，则尝试从 auth.getName() 获取
                // auth.getName() 通常是 username，也可能被你们实现为 userId（字符串）
                String name = auth.getName();
                if (StringUtils.hasText(name)) {
                    try {
                        // 如果 name 是纯数字字符串，则解析为 userId
                        return Long.parseLong(name);
                    } catch (NumberFormatException ignored) {
                        // name 不是数字（例如 "admin"），则忽略，继续走 DEV 兜底
                    }
                }
            }
        } catch (Exception ignored) {
            // 容错：任何安全上下文读取异常都不影响主流程，继续走 DEV 兜底
        }

        // -------------------------
        // 2) DEV 兜底：从请求头读取用户 ID（仅用于本地调试/未接入认证时）
        // -------------------------
        try {
            // RequestContextHolder 能拿到当前线程绑定的请求上下文（需要在 Web 请求线程内）
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;

            // 获取 HttpServletRequest
            HttpServletRequest req = attrs.getRequest();
            if (req == null) return null;

            // 优先读取 X-User-Id（约定请求头）
            String v = req.getHeader("X-User-Id");

            // 如果没有，再读取 X-Dev-User-Id（另一种约定请求头）
            if (!StringUtils.hasText(v)) {
                v = req.getHeader("X-Dev-User-Id");
            }

            // 有值则尝试解析为 Long
            if (StringUtils.hasText(v)) {
                try {
                    return Long.parseLong(v.trim());
                } catch (NumberFormatException ignored) {
                    // 请求头不是数字，忽略并返回 null
                }
            }
        } catch (Exception ignored) {
            // 容错：没有请求上下文/非 Web 线程/其它异常都直接返回 null
        }

        // 两种方式都取不到，返回 null
        return null;
    }
}
