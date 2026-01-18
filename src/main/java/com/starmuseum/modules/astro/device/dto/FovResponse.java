// src/main/java/com/starmuseum/modules/astro/device/dto/FovResponse.java
package com.starmuseum.modules.astro.device.dto;

import lombok.Data;

/**
 * Phase 5A：FOV 计算响应
 */
@Data
public class FovResponse {

    /**
     * PHOTO / VISUAL
     */
    private String type;

    /**
     * 水平视场角（度）
     * - PHOTO: 使用 sensorWidth 计算
     * - VISUAL: 等于 tfovDeg
     */
    private Double horizontalDeg;

    /**
     * 垂直视场角（度）
     * - PHOTO: 使用 sensorHeight 计算
     * - VISUAL: 等于 tfovDeg
     */
    private Double verticalDeg;

    /**
     * 对角线视场角（度）
     * - PHOTO: 使用传感器对角线计算
     * - VISUAL: 等于 tfovDeg
     */
    private Double diagonalDeg;

    /**
     * True Field Of View（度）
     * - PHOTO: 等于 diagonalDeg（MVP）
     * - VISUAL: eyepieceAfov / magnification
     */
    private Double tfovDeg;

    /**
     * 目视倍率（VISUAL）：telescopeFocal / eyepieceFocal
     * PHOTO 下为 null
     */
    private Double magnification;

    /**
     * 画面框旋转角（度）
     */
    private Double rotationDeg;

    /**
     * 画面框宽/高（度），用于前端叠加绘制
     */
    private Double frameWidthDeg;
    private Double frameHeightDeg;
}
