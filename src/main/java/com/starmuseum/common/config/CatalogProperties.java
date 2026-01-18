package com.starmuseum.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalog 配置
 *
 * 支持两套字段名：
 * 1) catalog.resource-base-url   （推荐/历史默认）
 * 2) catalog.public-path         （兼容旧写法）
 */
@Data
@Component
@ConfigurationProperties(prefix = "catalog")
public class CatalogProperties {

    /**
     * 资源存储根目录（磁盘路径）
     * 例：D:/data/starmuseum/catalog_storage
     */
    private String storageDir;

    /**
     * 导入临时目录
     * 例：D:/data/starmuseum/tmp/catalog-import
     */
    private String tempDir;

    /**
     * 资源访问前缀（推荐/历史默认）
     * 例：/catalog
     */
    private String resourceBaseUrl;

    /**
     * 资源访问前缀（兼容字段）
     * 例：/catalog
     *
     * 如果你 yml 用的是 public-path，这里会被绑定
     */
    private String publicPath;

    /**
     * 可选：用于拼绝对 URL（接口返回用）
     * 例：http://localhost:8080
     */
    private String baseUrl;

    private Textures textures = new Textures();

    /**
     * 返回“最终生效”的资源访问前缀
     * 优先 resourceBaseUrl，其次 publicPath，最后默认 /catalog
     */
    public String effectiveResourceBaseUrl() {
        if (StringUtils.hasText(resourceBaseUrl)) {
            return normalizePath(resourceBaseUrl);
        }
        if (StringUtils.hasText(publicPath)) {
            return normalizePath(publicPath);
        }
        return "/catalog";
    }

    /**
     * 返回“最终生效”的 baseUrl（去掉末尾 /）
     */
    public String effectiveBaseUrl() {
        if (!StringUtils.hasText(baseUrl)) return null;
        String s = baseUrl.trim();
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private String normalizePath(String p) {
        String s = p.trim();
        if (!s.startsWith("/")) s = "/" + s;
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    @Data
    public static class Textures {
        /**
         * 分辨率档位：2k/4k/8k
         */
        private List<String> resolutions = new ArrayList<>();

        /**
         * 支持扩展：webp/jpg/png...
         */
        private List<String> extensions = new ArrayList<>();
    }
}
