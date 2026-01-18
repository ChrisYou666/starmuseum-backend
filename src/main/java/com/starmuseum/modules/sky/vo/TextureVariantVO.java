package com.starmuseum.modules.sky.vo;

import lombok.Data;

@Data
public class TextureVariantVO {
    private String resolution;   // 2k/4k/8k
    private boolean available;   // 是否存在
    private String url;          // 可访问 URL（存在时）
    private Long sizeBytes;      // 文件大小（存在时）
    private String etag;         // 简单 ETag（存在时）
}
