package com.starmuseum.modules.astro.vo;

import lombok.Data;

@Data
public class BodyDetailVO {

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

    /**
     * Phase 5D：扩展字段 JSON（角直径、尺寸、类型等）
     * - 前端可直接 JSON.parse(extraJson)
     */
    private String extraJson;

    private Double altitudeDeg;
    private Double azimuthDeg;
    private Boolean visible;
}
