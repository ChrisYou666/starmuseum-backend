package com.starmuseum.common.security;

import com.starmuseum.common.config.AdminProperties;
import com.starmuseum.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理员身份识别（阶段3：最小闭环）
 *
 * 说明：
 * - 当前不做复杂 RBAC，只通过配置的 userIds 判断是否为管理员。
 * - 后续阶段4可以替换成 JWT authorities / user_role 表等更企业化方案。
 */
@Component
public class AdminGuard {

    private final AdminProperties props;

    public AdminGuard(AdminProperties props) {
        this.props = props;
    }

    /**
     * 是否为管理员
     */
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        List<Long> adminIds = props.getUserIds();
        return adminIds != null && adminIds.contains(userId);
    }

    /**
     * 要求必须是管理员，否则抛出 403
     */
    public void requireAdmin(Long userId) {
        if (!isAdmin(userId)) {
            throw new BizException(403, "无管理员权限");
        }
    }
}
