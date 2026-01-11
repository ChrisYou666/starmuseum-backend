package com.starmuseum.common.enums;

/**
 * 位置可见性等级（阶段3）
 */
public enum LocationVisibility {
    /**
     * 完全隐藏（不返回城市也不返回坐标）
     */
    HIDDEN,

    /**
     * 仅显示城市（不返回坐标）
     */
    CITY,

    /**
     * 模糊坐标（返回降精度坐标，可选返回城市）
     */
    FUZZY,

    /**
     * 精确坐标（仅对本人可见；对他人会按策略降级）
     */
    EXACT
}
