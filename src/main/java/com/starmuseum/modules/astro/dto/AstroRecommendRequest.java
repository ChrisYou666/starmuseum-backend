package com.starmuseum.modules.astro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AstroRecommendRequest {

    /**
     * ISO-8601 且带时区，例如：2026-01-10T12:00:00Z
     */
    @NotBlank
    private String time;

    @NotNull
    private Double lat;

    @NotNull
    private Double lon;

    @Min(1)
    @Max(50)
    private Integer top = 20;

    /**
     * 是否允许返回不可见目标（默认 false：尽量给“可见”的）
     */
    private Boolean includeNotVisible = false;
}
