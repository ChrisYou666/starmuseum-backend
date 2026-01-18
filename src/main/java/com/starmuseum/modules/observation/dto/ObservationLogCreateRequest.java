package com.starmuseum.modules.observation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ObservationLogCreateRequest {

    @NotNull(message = "observedAt 不能为空")
    private LocalDateTime observedAt;

    /**
     * PHOTO / VISUAL / OTHER
     */
    @NotBlank(message = "method 不能为空")
    private String method;

    private Long deviceProfileId;

    @Size(max = 2000, message = "notes 最大 2000 字符")
    private String notes;

    /**
     * 1=成功 0=失败（可选）
     */
    @Min(value = 0, message = "success 只能是 0 或 1")
    @Max(value = 1, message = "success 只能是 0 或 1")
    private Integer success;

    /**
     * 1~5（可选）
     */
    @Min(value = 1, message = "rating 最小 1")
    @Max(value = 5, message = "rating 最大 5")
    private Integer rating;

    /**
     * EXACT / FUZZY / HIDDEN
     */
    @NotBlank(message = "locationVisibility 不能为空")
    private String locationVisibility;

    /**
     * EXACT/FUZZY 时可传
     */
    private Double lat;
    private Double lon;

    @Size(max = 64, message = "cityName 最大 64 字符")
    private String cityName;

    @Valid
    private List<ObservationTargetInput> targets;

    /**
     * 复用 media 表的 id
     */
    private List<Long> mediaIds;
}
