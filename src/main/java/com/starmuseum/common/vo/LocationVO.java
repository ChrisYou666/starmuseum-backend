package com.starmuseum.common.vo;

import com.starmuseum.common.enums.LocationVisibility;
import lombok.Data;

/**
 * 位置返回（按隐私规则处理后的最终结果）
 */
@Data
public class LocationVO {

    /**
     * 对“当前观看者”而言的可见性（EXACT 可能被降级为 FUZZY / CITY）
     */
    private LocationVisibility visibility;

    /**
     * 城市名（可选）
     */
    private String cityName;

    /**
     * 坐标（可能为空，或为降精度坐标）
     */
    private Double lat;
    private Double lon;

    public static LocationVO city(String cityName) {
        LocationVO vo = new LocationVO();
        vo.setVisibility(LocationVisibility.CITY);
        vo.setCityName(cityName);
        return vo;
    }

    public static LocationVO fuzzy(String cityName, Double lat, Double lon) {
        LocationVO vo = new LocationVO();
        vo.setVisibility(LocationVisibility.FUZZY);
        vo.setCityName(cityName);
        vo.setLat(lat);
        vo.setLon(lon);
        return vo;
    }

    public static LocationVO exact(String cityName, Double lat, Double lon) {
        LocationVO vo = new LocationVO();
        vo.setVisibility(LocationVisibility.EXACT);
        vo.setCityName(cityName);
        vo.setLat(lat);
        vo.setLon(lon);
        return vo;
    }
}
