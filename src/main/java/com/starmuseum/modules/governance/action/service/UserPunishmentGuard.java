package com.starmuseum.modules.governance.action.service;

import com.starmuseum.common.exception.BizException;
import com.starmuseum.iam.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserPunishmentGuard {

    public void checkCanLogin(User user) {
        if (user == null) return;
        if (user.getBanned() != null && user.getBanned() == 1) {
            throw new BizException(403, "你已被永久封禁，无法登录");
        }
        if (user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(403, "你已被封禁至 " + user.getSuspendedUntil() + "，无法登录");
        }
    }

    public void checkCanPost(User user) {
        checkMute(user, "发帖");
    }

    public void checkCanComment(User user) {
        checkMute(user, "评论");
    }

    private void checkMute(User user, String actionName) {
        if (user == null) return;
        if (user.getMutedUntil() != null && user.getMutedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(403, "你已被禁言至 " + user.getMutedUntil() + "，无法" + actionName);
        }
    }
}
