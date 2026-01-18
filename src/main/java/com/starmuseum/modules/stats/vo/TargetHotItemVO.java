package com.starmuseum.modules.stats.vo;

import lombok.Data;

/**
 * 热门目标统计项（个人/社区通用）
 */
@Data
public class TargetHotItemVO {

    private String targetType; // CELESTIAL_BODY / TEXT
    private Long targetId;      // 可为空（TEXT 一般为空）
    private String targetName;
    private String bodyType;    // 例如 DSO/STAR/PLANET（可为空）

    private Long cnt;
}
