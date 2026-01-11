package com.starmuseum.modules.astro.vo;

import lombok.Data;

/**
 * 天体详情返回（/api/astro/body/{id}）
 */
@Data
public class CelestialBodyDetailResponse {

    private Long id;

    private String catalogCode;
    private String bodyType;

    private String name;
    private String nameZh;
    private String nameEn;
    private String nameId;

    private Double raDeg;
    private Double decDeg;

    private Double mag;

    private String spectralType;
    private String constellation;
    private String wikiUrl;

    /** 当前时刻当前位置：高度角/方位角/是否可见 */
    private Double altitudeDeg;
    private Double azimuthDeg;
    private Boolean visible;
}
