package com.starmuseum.modules.astro.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高度角/方位角计算结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AltAzResult {

    /**
     * 高度角（deg）
     */
    private Double altitudeDeg;

    /**
     * 方位角（deg，0~360）
     */
    private Double azimuthDeg;

    /**
     * 是否可见（MVP：altitude > 0）
     */
    private boolean visible;

    public static AltAzResult of(Double altitudeDeg, Double azimuthDeg, boolean visible) {
        return new AltAzResult(altitudeDeg, azimuthDeg, visible);
    }
}
