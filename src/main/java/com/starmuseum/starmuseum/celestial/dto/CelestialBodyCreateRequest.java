package com.starmuseum.starmuseum.celestial.dto;

import com.starmuseum.starmuseum.celestial.enums.CelestialType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CelestialBodyCreateRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    /** 别名/中文名（可空） */
    private String alias;

    @NotNull(message = "类型不能为空")
    private CelestialType type;

    private String constellation;

    @Min(value = 0, message = "raHours 必须在 0~24 之间")
    @Max(value = 24, message = "raHours 必须在 0~24 之间")
    private Double raHours;

    @Min(value = -90, message = "decDegrees 必须在 -90~90 之间")
    @Max(value = 90, message = "decDegrees 必须在 -90~90 之间")
    private Double decDegrees;

    /** 视星等一般不会太离谱，给个宽松范围 */
    @Min(value = -30, message = "magnitude 范围不合理")
    @Max(value = 30, message = "magnitude 范围不合理")
    private Double magnitude;

    @Min(value = 0, message = "distanceLy 不能为负数")
    private Double distanceLy;

    private String spectralType;

    @Min(value = 0, message = "temperatureK 不能为负数")
    private Integer temperatureK;

    private String description;
}