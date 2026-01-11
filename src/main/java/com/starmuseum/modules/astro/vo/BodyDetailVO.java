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

    private Double altitudeDeg;
    private Double azimuthDeg;
    private Boolean visible;
}
