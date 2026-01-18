package com.starmuseum.modules.catalog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "starmuseum.catalog")
@Data
public class CatalogStorageProperties {

    /**
     * catalog zip 包落盘目录（可选，便于审计/复用）
     * 例如：D:/data/starmuseum/catalog-packages
     */
    private String packageDir;

    /**
     * 导入解压临时目录（可写）
     * 例如：D:/data/starmuseum/tmp/catalog-import
     */
    private String tempDir;
}
