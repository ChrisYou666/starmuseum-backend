package com.starmuseum.modules.governance.action.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.modules.governance.action.dto.ModerationActionCreateRequest;
import com.starmuseum.modules.governance.action.entity.ModerationAction;
import com.starmuseum.modules.governance.action.enums.ModerationActionType;
import com.starmuseum.modules.governance.action.mapper.ModerationActionMapper;
import com.starmuseum.modules.governance.action.service.ModerationActionService;
import com.starmuseum.modules.post.entity.Post;
import com.starmuseum.modules.post.entity.PostComment;
import com.starmuseum.modules.post.mapper.PostCommentMapper;
import com.starmuseum.modules.post.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ModerationActionServiceImpl implements ModerationActionService {

    private final ModerationActionMapper actionMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;

    public ModerationActionServiceImpl(ModerationActionMapper actionMapper,
                                       UserMapper userMapper,
                                       PostMapper postMapper,
                                       PostCommentMapper postCommentMapper) {
        this.actionMapper = actionMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
    }

    @Override
    @Transactional
    public void apply(ModerationActionCreateRequest req, Long operatorUserId, Long relatedReportId) {
        if (req == null) {
            throw new BizException(400, "action request is null");
        }
        if (operatorUserId == null) {
            throw new BizException(401, "operator not login");
        }
        if (!StringUtils.hasText(req.getActionType())) {
            throw new BizException(400, "actionType is required");
        }

        ModerationActionType type;
        try {
            type = ModerationActionType.valueOf(req.getActionType().trim().toUpperCase());
        } catch (Exception e) {
            throw new BizException(400, "invalid actionType: " + req.getActionType());
        }

        // 1) 先校验 + 执行副作用（更新 user/post/comment）
        switch (type) {
            case WARN -> {
                // WARN：仅记录，不改业务数据
                if (req.getTargetUserId() == null) {
                    throw new BizException(400, "WARN requires targetUserId");
                }
                ensureUserExists(req.getTargetUserId());
            }

            case BAN -> {
                if (req.getTargetUserId() == null) {
                    throw new BizException(400, "BAN requires targetUserId");
                }
                User u = ensureUserExists(req.getTargetUserId());
                u.setBanned(1);
                userMapper.updateById(u);
            }

            case SUSPEND -> {
                if (req.getTargetUserId() == null) {
                    throw new BizException(400, "SUSPEND requires targetUserId");
                }
                if (req.getDurationHours() == null || req.getDurationHours() <= 0) {
                    throw new BizException(400, "SUSPEND requires durationHours > 0");
                }
                User u = ensureUserExists(req.getTargetUserId());
                u.setSuspendedUntil(LocalDateTime.now().plusHours(req.getDurationHours()));
                userMapper.updateById(u);
            }

            case MUTE -> {
                if (req.getTargetUserId() == null) {
                    throw new BizException(400, "MUTE requires targetUserId");
                }
                if (req.getDurationHours() == null || req.getDurationHours() <= 0) {
                    throw new BizException(400, "MUTE requires durationHours > 0");
                }
                User u = ensureUserExists(req.getTargetUserId());
                u.setMutedUntil(LocalDateTime.now().plusHours(req.getDurationHours()));
                userMapper.updateById(u);
            }

            case DELETE_CONTENT -> {
                if (!StringUtils.hasText(req.getTargetType())) {
                    throw new BizException(400, "DELETE_CONTENT requires targetType=POST/COMMENT");
                }
                if (req.getTargetId() == null) {
                    throw new BizException(400, "DELETE_CONTENT requires targetId");
                }

                String tt = req.getTargetType().trim().toUpperCase();
                if ("POST".equals(tt)) {
                    softDeletePost(req.getTargetId(), operatorUserId);
                } else if ("COMMENT".equals(tt)) {
                    softDeleteComment(req.getTargetId());
                } else {
                    throw new BizException(400, "invalid targetType: " + req.getTargetType());
                }
            }
        }

        // 2) 写 action 记录（审计来源：relatedReportId + operator）
        ModerationAction a = new ModerationAction();
        a.setActionType(type.name());
        a.setTargetUserId(req.getTargetUserId());
        a.setTargetType(StringUtils.hasText(req.getTargetType()) ? req.getTargetType().trim().toUpperCase() : null);
        a.setTargetId(req.getTargetId());
        a.setDurationHours(req.getDurationHours());
        a.setReason(req.getReason());
        a.setOperatorUserId(operatorUserId);
        a.setRelatedReportId(relatedReportId);
        a.setCreatedAt(LocalDateTime.now());

        actionMapper.insert(a);
    }

    private User ensureUserExists(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(404, "user not found: " + userId);
        }
        return u;
    }

    private void softDeletePost(Long postId, Long operatorUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null) {
            return; // 已删除/不存在：按幂等处理
        }
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(operatorUserId);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
    }

    private void softDeleteComment(Long commentId) {
        PostComment c = postCommentMapper.selectById(commentId);
        if (c == null || Objects.equals(c.getDeleted(), 1)) {
            return;
        }
        // 你项目 comment 用 TableLogic：deleteById 会 update deleted=1
        postCommentMapper.deleteById(commentId);
    }
}
