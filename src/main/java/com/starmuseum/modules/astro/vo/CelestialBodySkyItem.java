package com.starmuseum.modules.astro.vo;

import lombok.Data;

/**
 * 星空列表/概览用的天体条目（用于 sky/summary 或 listSkyBodies）
 */
@Data
public class CelestialBodySkyItem {

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

    // ✅ 补齐这两个字段，否则 Service 里 set 会报错
    private String spectralType;
    private String wikiUrl;

    private String constellation;

    /** 当前时刻当前位置：高度角/方位角/是否可见 */
    private Double altitudeDeg;
    private Double azimuthDeg;
    private Boolean visible;
}
