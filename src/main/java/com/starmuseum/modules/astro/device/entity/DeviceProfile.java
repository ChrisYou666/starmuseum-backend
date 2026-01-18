// src/main/java/com/starmuseum/modules/astro/device/entity/DeviceProfile.java
package com.starmuseum.modules.astro.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 5A：设备配置（摄影 PHOTO / 目视 VISUAL）
 */
@Data
@TableName("astro_device_profile")
public class DeviceProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    /**
     * PHOTO / VISUAL
     */
    @TableField("type")
    private String type;

    // ===== PHOTO 参数（单位：mm） =====
    @TableField("sensor_width_mm")
    private Double sensorWidthMm;

    @TableField("sensor_height_mm")
    private Double sensorHeightMm;

    @TableField("focal_length_mm")
    private Double focalLengthMm;

    // ===== VISUAL 参数（单位：mm；AFOV 单位：deg） =====
    @TableField("telescope_focal_mm")
    private Double telescopeFocalMm;

    @TableField("eyepiece_focal_mm")
    private Double eyepieceFocalMm;

    @TableField("eyepiece_afov_deg")
    private Double eyepieceAfovDeg;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
