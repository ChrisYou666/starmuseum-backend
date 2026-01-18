// src/main/java/com/starmuseum/modules/astro/device/dto/DeviceProfileCreateRequest.java
package com.starmuseum.modules.astro.device.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Phase 5A：创建设备配置
 *
 * 说明：不同 type 的必填字段由后端业务校验（避免前端误传导致 400 难排查）。
 */
@Data
public class DeviceProfileCreateRequest {

    @NotBlank(message = "name 不能为空")
    @Size(max = 64, message = "name 最大 64 字符")
    private String name;

    /**
     * PHOTO / VISUAL
     */
    @NotBlank(message = "type 不能为空")
    private String type;

    // ===== PHOTO 参数 =====
    @Positive(message = "sensorWidthMm 必须为正数")
    private Double sensorWidthMm;

    @Positive(message = "sensorHeightMm 必须为正数")
    private Double sensorHeightMm;

    @Positive(message = "focalLengthMm 必须为正数")
    private Double focalLengthMm;

    // ===== VISUAL 参数 =====
    @Positive(message = "telescopeFocalMm 必须为正数")
    private Double telescopeFocalMm;

    @Positive(message = "eyepieceFocalMm 必须为正数")
    private Double eyepieceFocalMm;

    @Positive(message = "eyepieceAfovDeg 必须为正数")
    @Max(value = 180, message = "eyepieceAfovDeg 最大 180")
    private Double eyepieceAfovDeg;

    /**
     * 是否设置为默认（可选）
     */
    private Boolean setAsDefault;
}
