package com.starmuseum.modules.stats.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 我的统计返回（聚合）
 */
@Data
public class MyStatsResponse {

    private LocalDateTime from;
    private LocalDateTime to;

    /**
     * 观测日志数（范围内）
     */
    private Long logCount;

    /**
     * 已发布动态数（范围内，observation_log.published=1）
     */
    private Long publishedCount;

    /**
     * 成功次数（success=1）
     */
    private Long successCount;

    /**
     * 成功率（百分比，如 75.32），若 logCount=0 则为 null
     */
    private Double successRate;

    /**
     * 平均评分（rating 平均，保留2位），无评分则为 null
     */
    private Double avgRating;

    /**
     * 方式分布（PHOTO/VISUAL/OTHER）
     */
    private List<MethodCountVO> methodDistribution;

    /**
     * 常观测目标 TopN
     */
    private List<TargetHotItemVO> topTargets;
}
