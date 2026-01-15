package com.starmuseum.modules.block.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.block.service.UserBlockService;
import com.starmuseum.modules.block.vo.BlockActionResponse;
import com.starmuseum.modules.block.vo.BlockedUserVO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final UserBlockService userBlockService;

    public BlockController(UserBlockService userBlockService) {
        this.userBlockService = userBlockService;
    }

    @PostMapping("/{blockedUserId}")
    public BlockActionResponse block(@PathVariable Long blockedUserId) {
        Long userId = currentUserId();
        return userBlockService.block(userId, blockedUserId);
    }

    @DeleteMapping("/{blockedUserId}")
    public void unblock(@PathVariable Long blockedUserId) {
        Long userId = currentUserId();
        userBlockService.unblock(userId, blockedUserId);
    }

    @GetMapping
    public IPage<BlockedUserVO> myBlocks(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size) {
        Long userId = currentUserId();
        return userBlockService.pageMy(page, size, userId);
    }

    /**
     * 获取当前登录用户 id（反射兜底，适配 JWT principal）
     */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("未登录");
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof Long) return (Long) principal;

        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (Exception ignored) {
            }
        }

        try {
            Method m = principal.getClass().getMethod("getId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {
        }

        try {
            Method m = principal.getClass().getMethod("getUserId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {
        }

        try {
            Method m = principal.getClass().getMethod("getUsername");
            Object v = m.invoke(principal);
            if (v != null) return Long.parseLong(v.toString());
        } catch (Exception ignored) {
        }

        throw new AccessDeniedException("无法解析当前用户ID，请在 BlockController.currentUserId() 里适配你的 principal 类型");
    }
}
