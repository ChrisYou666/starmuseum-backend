package com.starmuseum.modules.governance.audit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.AdminGuard;
import com.starmuseum.modules.governance.audit.service.AuditLogService;
import com.starmuseum.modules.governance.audit.vo.AdminAuditLogVO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AuditLogService auditLogService;
    private final AdminGuard adminGuard;

    public AdminAuditController(AuditLogService auditLogService, AdminGuard adminGuard) {
        this.auditLogService = auditLogService;
        this.adminGuard = adminGuard;
    }

    /**
     * GET /api/admin/audit?page=&size=&operatorUserId=&action=&entityType=&entityId=&from=&to=
     *
     * from/to：建议传 ISO-8601，例如 2026-01-12T00:00:00
     */
    @GetMapping
    public Result<IPage<AdminAuditLogVO>> page(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) Long operatorUserId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) Long entityId,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to
    ) {
        Long currentUserId = currentUserId();
        adminGuard.requireAdmin(currentUserId);

        IPage<AdminAuditLogVO> auditLogs = auditLogService.getAuditLogs(
            page, size, operatorUserId, action, entityType, entityId, from, to
        );
        return Result.ok(auditLogs);
    }

    /**
     * 从 Spring Security 里拿当前用户 ID（反射兜底，保持与你其他模块一致）
     */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("未登录");
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof Long) return (Long) principal;

        if (principal instanceof String) {
            try { return Long.parseLong((String) principal); } catch (Exception ignored) {}
        }

        try {
            Method m = principal.getClass().getMethod("getId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {}

        try {
            Method m = principal.getClass().getMethod("getUserId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {}

        try {
            Method m = principal.getClass().getMethod("getUsername");
            Object v = m.invoke(principal);
            if (v != null) return Long.parseLong(v.toString());
        } catch (Exception ignored) {}

        throw new AccessDeniedException("无法解析当前用户ID");
    }
}
