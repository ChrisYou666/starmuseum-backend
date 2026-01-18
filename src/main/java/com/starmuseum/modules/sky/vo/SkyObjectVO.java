package com.starmuseum.modules.sky.vo;

import lombok.Data;

@Data
public class SkyObjectVO {
    private Long id;
    private String name;
    private String type;     // STAR/DSO/...
    private Double raDeg;    // 赤经（度）
    private Double decDeg;   // 赤纬（度）
    private Double mag;      // 视星等（越小越亮）
}
