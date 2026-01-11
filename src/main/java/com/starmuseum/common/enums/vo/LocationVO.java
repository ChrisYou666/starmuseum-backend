package com.starmuseum.common.vo;

import com.starmuseum.common.enums.LocationVisibility;
import lombok.Data;

/**
 * 通用位置返回结构（阶段3）
 *
 * 注意：
 * - lat/lon 可能为 null（例如 HIDDEN/CITY）
 * - FUZZY/EXACT 的实际返回由后端隐私规则决定（对他人可能降级）
 */
@Data
public class LocationVO {

    /**
     * 位置可见性
     */
    private LocationVisibility visibility;

    /**
     * 城市名（可选）
     */
    private String cityName;

    /**
     * 纬度（可能为 null 或模糊值）
     */
    private Double lat;

    /**
     * 经度（可能为 null 或模糊值）
     */
    private Double lon;
}
