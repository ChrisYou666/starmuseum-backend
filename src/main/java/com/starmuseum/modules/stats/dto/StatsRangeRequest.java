package com.starmuseum.modules.stats.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计查询范围
 * - from/to：建议传 ISO8601（无时区也可，按后端本地时区解析）
 * - top：可选，用于我的 Top 目标
 */
@Data
public class StatsRangeRequest {

    @NotNull
    private LocalDateTime from;

    @NotNull
    private LocalDateTime to;

    /**
     * 可选：TopN（默认 10，最大 50）
     */
    private Integer top;
}
