package com.starmuseum.modules.astro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkySummaryRequest {

    /**
     * ISO-8601 时间字符串，建议前端传 UTC，如：2026-01-10T12:00:00Z
     */
    @NotBlank
    private String time;

    @NotNull
    @Min(-90)
    @Max(90)
    private Double lat;

    @NotNull
    @Min(-180)
    @Max(180)
    private Double lon;

    /**
     * 返回多少颗星（默认 50）
     */
    @Min(1)
    @Max(500)
    private Integer limit;

    /**
     * 是否只返回可见（altitude > 0）的天体，默认 false
     */
    private Boolean visibleOnly;
    /**
     * 排序方式：
     * - mag: 按星等（亮到暗，默认）
     * - alt: 按高度（高到低）
     */
    private String sort;
}
