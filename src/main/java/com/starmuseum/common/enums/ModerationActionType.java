package com.starmuseum.common.enums;

/**
 * 内容治理处置动作类型（阶段3）
 */
public enum ModerationActionType {
    /**
     * 警告
     */
    WARN,

    /**
     * 删除内容（软删除帖子/评论等）
     */
    DELETE_CONTENT,

    /**
     * 禁言（限制发帖/评论）
     */
    MUTE,

    /**
     * 暂停登录（限制登录）
     */
    SUSPEND,

    /**
     * 永久封禁
     */
    BAN
}
