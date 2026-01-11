package com.starmuseum.common.enums;

/**
 * 举报单状态（阶段3）
 */
public enum ReportStatus {
    /**
     * 已提交，待处理
     */
    OPEN,

    /**
     * 管理员处理中
     */
    IN_REVIEW,

    /**
     * 已处理并认定成立
     */
    RESOLVED,

    /**
     * 已处理但驳回（不成立）
     */
    REJECTED,

    /**
     * 举报人撤回
     */
    WITHDRAWN
}
