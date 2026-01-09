package com.starmuseum.modules.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体存储相关配置（Step C）
 * application-dev.yml:
 * starmuseum.storage.local.upload-dir
 * starmuseum.storage.public-base-url
 * starmuseum.storage.image.*
 */
@ConfigurationProperties(prefix = "starmuseum.storage")
@Data
public class StorageProperties {

    /**
     * 本地上传根目录（物理路径）
     * 例如：D:/data/starmuseum/uploads
     * 最终 URL 会映射为：/uploads/**
     */
    private Local local = new Local();

    /**
     * 对外可访问的 baseUrl，例如：http://localhost:8080
     * 用于拼接 origin/thumb/medium URL
     */
    private String publicBaseUrl;

    /**
     * 图片处理参数
     */
    private Image image = new Image();

    @Data
    public static class Local {
        private String uploadDir;
    }

    @Data
    public static class Image {

        /**
         * 允许上传的 mime types
         */
        private List<String> allowedMimeTypes = new ArrayList<>();

        /**
         * 单文件最大字节数（兜底校验）
         */
        private long maxFileSizeBytes = 10 * 1024 * 1024;

        /**
         * 缩略图最大边（保持比例）
         */
        private int thumbMaxSide = 320;

        /**
         * 中等图最大边（保持比例）
         */
        private int mediumMaxSide = 1280;

    }
}
