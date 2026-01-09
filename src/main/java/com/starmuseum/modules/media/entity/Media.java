package com.starmuseum.modules.media.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("media")
@Data
public class Media {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String bizType;

    private String originUrl;

    private String thumbUrl;

    private String mediumUrl;

    private String mimeType;

    private Long sizeBytes;

    private Integer width;

    private Integer height;

    private String sha256;

    private String storageType;

    private String storageKey;

    private LocalDateTime createdAt;
}
