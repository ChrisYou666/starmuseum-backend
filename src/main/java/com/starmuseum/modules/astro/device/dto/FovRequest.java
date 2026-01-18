// src/main/java/com/starmuseum/modules/astro/device/dto/FovRequest.java
package com.starmuseum.modules.astro.device.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Phase 5A：FOV 计算请求
 *
 * 支持两种用法：
 * 1) profileId：从数据库读取设备配置计算
 * 2) 直传参数：不落库直接计算
 */
@Data
public class FovRequest {

    /**
     * 可选：设备配置 ID（如果传了，优先以 profile 为准）
     */
    private Long profileId;

    /**
     * 可选：type（PHOTO / VISUAL）。
     * - 当 profileId 为空时必填
     */
    private String type;

    // ===== PHOTO 参数（单位：mm） =====
    @Positive(message = "sensorWidthMm 必须为正数")
    private Double sensorWidthMm;

    @Positive(message = "sensorHeightMm 必须为正数")
    private Double sensorHeightMm;

    /**
     * 镜头焦距（mm）
     */
    @Positive(message = "focalLengthMm 必须为正数")
    private Double focalLengthMm;

    // ===== VISUAL 参数（单位：mm；AFOV 单位：deg） =====
    @Positive(message = "telescopeFocalMm 必须为正数")
    private Double telescopeFocalMm;

    @Positive(message = "eyepieceFocalMm 必须为正数")
    private Double eyepieceFocalMm;

    @Positive(message = "eyepieceAfovDeg 必须为正数")
    @Max(value = 180, message = "eyepieceAfovDeg 最大 180")
    private Double eyepieceAfovDeg;

    /**
     * 画面框旋转角（度），前端暂时可以不传，默认 0。
     */
    @Min(value = -180, message = "rotationDeg 最小 -180")
    @Max(value = 180, message = "rotationDeg 最大 180")
    private Double rotationDeg;
}
