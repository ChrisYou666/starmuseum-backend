package com.starmuseum.modules.sky.vo;

import lombok.Data;

import java.util.List;

/**
 * objects 按需加载响应
 */
@Data
public class SkyObjectsResponse {

    /**
     * 请求版本：active / v2026_01 ...
     */
    private String version;

    /**
     * 对象类型：STAR/DSO...
     */
    private String type;

    /**
     * 实际返回数量
     */
    private Integer count;

    /**
     * 数据列表
     */
    private List<SkyObjectItemVO> items;
}
