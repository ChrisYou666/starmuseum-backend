package com.starmuseum.starmuseum.celestial.dto;

import com.starmuseum.starmuseum.celestial.enums.CelestialType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CelestialBodyQueryRequest {

    /** 关键字：模糊匹配 name / alias / constellation / spectralType / description */
    private String keyword;

    /** 类型（可空） */
    private CelestialType type;

    /** 星座（可空） */
    private String constellation;

    /** 最小视星等（可空） */
    @Min(value = -30, message = "minMagnitude 范围不合理")
    @Max(value = 30, message = "minMagnitude 范围不合理")
    private Double minMagnitude;

    /** 最大视星等（可空） */
    @Min(value = -30, message = "maxMagnitude 范围不合理")
    @Max(value = 30, message = "maxMagnitude 范围不合理")
    private Double maxMagnitude;

    /**
     * 排序字段（可空）：
     * - created_at（默认）
     * - magnitude
     * - distance_ly
     */
    private String orderBy;
}