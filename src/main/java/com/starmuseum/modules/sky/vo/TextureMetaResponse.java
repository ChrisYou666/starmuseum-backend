package com.starmuseum.modules.sky.vo;

import lombok.Data;

import java.util.List;

/**
 * 贴图元数据响应
 */
@Data
public class TextureMetaResponse {

    /**
     * 资源场景/贴图组：例如 milkyway
     */
    private String scene;

    /**
     * 生效的资源访问前缀：例如 /catalog
     */
    private String resourceBaseUrl;

    /**
     * 生效的存储根目录：例如 D:/data/.../catalog_storage
     */
    private String storageDir;

    /**
     * 分档列表（2k/4k/8k）
     */
    private List<TextureLevelVO> levels;
}
