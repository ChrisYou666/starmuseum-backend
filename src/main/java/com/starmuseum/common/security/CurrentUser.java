package com.starmuseum.common.security;

import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * 从 Spring Security 的上下文中获取当前登录用户信息。
 * 这里主要取 userId（用于 media.user_id）。
 *
 * 兼容策略：
 * - principal 是 Long/Integer：直接返回
 * - authentication.getName() 是数字字符串：解析成 Long
 *
 * 如果你后面把 principal 设计成自定义 UserPrincipal，也可以在这里扩展。
 */
@Data
public class CurrentUser {

    private CurrentUser() {}

    public static Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Long l) {
            return l;
        }
        if (principal instanceof Integer i) {
            return i.longValue();
        }

        String name = auth.getName();
        if (StringUtils.hasText(name)) {
            try {
                return Long.parseLong(name);
            } catch (NumberFormatException ignored) {
            }
        }

        throw new IllegalStateException("Cannot resolve current userId from SecurityContext");
    }
}
