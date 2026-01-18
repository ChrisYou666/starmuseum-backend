package com.starmuseum.modules.sky.vo;

import lombok.Data;

/**
 * 天体对象（按需加载最小字段集）
 * 先保证首屏能用：id + name + ra/dec + mag（亮度）+ type
 */
@Data
public class SkyObjectItemVO {

    private Long id;

    /**
     * STAR / DSO / PLANET ...
     */
    private String type;

    /**
     * 展示名（优先 name -> en_name -> zh_name）
     */
    private String name;

    /**
     * 赤经（度）
     */
    private Double raDeg;

    /**
     * 赤纬（度）
     */
    private Double decDeg;

    /**
     * 视星等（越小越亮）
     */
    private Double mag;
}
