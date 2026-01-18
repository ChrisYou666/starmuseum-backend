// src/main/java/com/starmuseum/modules/astro/device/vo/DeviceProfileVO.java
package com.starmuseum.modules.astro.device.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceProfileVO {

    private Long id;
    private Long userId;
    private String name;
    private String type;

    // PHOTO
    private Double sensorWidthMm;
    private Double sensorHeightMm;
    private Double focalLengthMm;

    // VISUAL
    private Double telescopeFocalMm;
    private Double eyepieceFocalMm;
    private Double eyepieceAfovDeg;

    private Integer isDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
