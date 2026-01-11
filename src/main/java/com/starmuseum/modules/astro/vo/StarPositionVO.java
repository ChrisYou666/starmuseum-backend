package com.starmuseum.modules.astro.vo;

import lombok.Data;

@Data
public class StarPositionVO {

    private Long id;
    private String catalogCode;

    private String name;
    private Double mag;

    private Double raDeg;
    private Double decDeg;

    private Double altitudeDeg;
    private Double azimuthDeg;

    private Boolean visible;
}
