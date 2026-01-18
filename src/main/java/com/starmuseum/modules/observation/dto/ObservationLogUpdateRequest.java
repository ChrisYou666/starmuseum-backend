package com.starmuseum.modules.observation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ObservationLogUpdateRequest {

    private LocalDateTime observedAt;

    /**
     * 不允许修改 method（保持一致性）；如确实需要可放开
     */
    private String method;

    private Long deviceProfileId;

    @Size(max = 2000, message = "notes 最大 2000 字符")
    private String notes;

    @Min(value = 0, message = "success 只能是 0 或 1")
    @Max(value = 1, message = "success 只能是 0 或 1")
    private Integer success;

    @Min(value = 1, message = "rating 最小 1")
    @Max(value = 5, message = "rating 最大 5")
    private Integer rating;

    private String locationVisibility;
    private Double lat;
    private Double lon;

    @Size(max = 64, message = "cityName 最大 64 字符")
    private String cityName;

    @Valid
    private List<ObservationTargetInput> targets;

    private List<Long> mediaIds;
}
