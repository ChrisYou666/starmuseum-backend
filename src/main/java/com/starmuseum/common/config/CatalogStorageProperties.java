package com.starmuseum.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
public class CatalogStorageProperties {

    /**
     * 资源根目录（磁盘目录）
     * 例如：D:/data/starmuseum/catalog_storage
     */
    private String storageDir;

    /**
     * 导入解压临时目录（磁盘目录）
     */
    private String tempDir;

    /**
     * 静态资源对外访问前缀
     * 例如：/catalog
     */
    private String resourceBaseUrl = "/catalog";

    private Textures textures = new Textures();

    @Data
    public static class Textures {
        /**
         * 支持的分辨率档位
         * 默认：2k/4k/8k
         */
        private List<String> resolutions = new ArrayList<>(Arrays.asList("2k", "4k", "8k"));

        /**
         * 支持的文件扩展名（按优先级尝试）
         * 默认：webp/jpg/jpeg/png
         */
        private List<String> extensions = new ArrayList<>(Arrays.asList("webp", "jpg", "jpeg", "png"));
    }
}
