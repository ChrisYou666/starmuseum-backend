package com.starmuseum.modules.stats.vo;

import lombok.Data;

/**
 * 方式分布统计项
 */
@Data
public class MethodCountVO {

    private String method; // PHOTO/VISUAL/OTHER
    private Long cnt;
}
