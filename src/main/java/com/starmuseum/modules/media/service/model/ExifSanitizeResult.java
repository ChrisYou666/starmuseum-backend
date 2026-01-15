package com.starmuseum.modules.media.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * EXIF 清理检测结果（只是数据，不是接口）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExifSanitizeResult {

    /**
     * 是否已执行清理（重编码输出，理论上为 true）
     */
    private boolean exifStripped;

    /**
     * 是否发现 GPS 信息
     */
    private boolean exifHasGps;

    /**
     * 是否发现设备信息（Make/Model 等）
     */
    private boolean exifHasDevice;

    /**
     * 检测时间
     */
    private LocalDateTime checkedAt;

}
