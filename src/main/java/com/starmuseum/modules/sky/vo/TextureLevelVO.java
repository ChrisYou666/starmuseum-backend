package com.starmuseum.modules.sky.vo;

import lombok.Data;

/**
 * 单个分辨率档位的贴图信息
 */
@Data
public class TextureLevelVO {

    /**
     * 档位名称：2k/4k/8k
     */
    private String resolution;

    /**
     * 是否存在
     */
    private boolean exists;

    /**
     * 相对访问路径（从 resourceBaseUrl 起算），例：/textures/milkyway/2k.webp
     */
    private String path;

    /**
     * 可访问 URL（如果配置了 catalog.base-url，则返回绝对 URL）
     */
    private String url;

    /**
     * 文件大小（字节）
     */
    private Long sizeBytes;

    /**
     * SHA-256（用于校验/缓存控制），不存在则为 null
     */
    private String sha256;
}
