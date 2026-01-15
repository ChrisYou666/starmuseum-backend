package com.starmuseum.common.controller;

import com.starmuseum.common.security.AdminGuard;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开发自检接口：验证 AdminGuard 配置是否生效
 *
 * 只在 dev 环境启用：spring.profiles.active=dev
 *
 * 用法：
 *  GET /api/dev/admin-check?userId=1
 *  GET /api/dev/admin-check?userId=2
 */
@Profile("dev")
@RestController
@RequestMapping("/api/dev")
public class DevAdminCheckController {

    private final AdminGuard adminGuard;

    public DevAdminCheckController(AdminGuard adminGuard) {
        this.adminGuard = adminGuard;
    }

    @GetMapping("/admin-check")
    public Map<String, Object> adminCheck(@RequestParam("userId") Long userId) {
        boolean isAdmin = adminGuard.isAdmin(userId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("userId", userId);
        res.put("isAdmin", isAdmin);
        return res;
    }
}
