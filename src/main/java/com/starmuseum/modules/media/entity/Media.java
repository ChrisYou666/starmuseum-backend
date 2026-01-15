package com.starmuseum.modules.media.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("media")
public class Media {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("biz_type")
    private String bizType;

    @TableField("origin_url")
    private String originUrl;

    @TableField("thumb_url")
    private String thumbUrl;

    @TableField("medium_url")
    private String mediumUrl;

    @TableField("mime_type")
    private String mimeType;

    @TableField("size_bytes")
    private Long sizeBytes;

    @TableField("width")
    private Integer width;

    @TableField("height")
    private Integer height;

    @TableField("sha256")
    private String sha256;

    @TableField("storage_type")
    private String storageType;

    @TableField("storage_key")
    private String storageKey;

    @TableField("created_at")
    private LocalDateTime createdAt;

    // ===== 3.3 EXIF 治理字段 =====
    @TableField("exif_stripped")
    private Integer exifStripped;

    @TableField("exif_has_gps")
    private Integer exifHasGps;

    @TableField("exif_has_device")
    private Integer exifHasDevice;

    @TableField("exif_checked_at")
    private LocalDateTime exifCheckedAt;
}
