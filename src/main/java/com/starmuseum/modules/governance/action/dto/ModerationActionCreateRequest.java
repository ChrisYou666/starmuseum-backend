package com.starmuseum.modules.governance.action.dto;

import lombok.Data;

/**
 * 管理员在审核认定后可附带的处罚动作
 */
@Data
public class ModerationActionCreateRequest {

    /**
     * WARN / DELETE_CONTENT / MUTE / SUSPEND / BAN
     */
    private String actionType;

    /**
     * 目标用户（WARN/MUTE/SUSPEND/BAN 必填；DELETE_CONTENT 通常从 content 作者推导，也可传）
     */
    private Long targetUserId;

    /**
     * DELETE_CONTENT 时使用：POST / COMMENT
     */
    private String targetType;

    /**
     * DELETE_CONTENT 时使用：postId/commentId
     */
    private Long targetId;

    /**
     * MUTE/SUSPEND：持续小时数（例如 24*3 = 72）
     */
    private Integer durationHours;

    /**
     * 原因描述（建议带上举报原因/备注）
     */
    private String reason;
}
