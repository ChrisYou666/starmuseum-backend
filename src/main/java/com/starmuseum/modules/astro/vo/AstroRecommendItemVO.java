package com.starmuseum.modules.astro.vo;

import lombok.Data;

@Data
public class AstroRecommendItemVO {

    private Long id;
    private String catalogCode;
    private String bodyType;

    private String name;
    private String nameZh;
    private String nameEn;
    private String nameId;

    private Double mag;
    private String constellation;

    private Double altitudeDeg;
    private Double azimuthDeg;
    private Boolean visible;

    /**
     * 综合分（方便前端调试/解释）
     */
    private Double score;

    /**
     * 推荐原因：PERSONAL / COMMUNITY / MIX
     */
    private String reason;
}
