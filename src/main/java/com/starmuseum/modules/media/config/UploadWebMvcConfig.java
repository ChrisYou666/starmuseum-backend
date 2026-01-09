package com.starmuseum.modules.media.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将物理目录映射成 URL：
 * /uploads/**  ->  file:{uploadDir}/
 *
 * 例如 uploadDir = D:/data/starmuseum/uploads
 * 则访问 http://localhost:8080/uploads/2026/01/09/xxx.jpg 会读取
 * D:/data/starmuseum/uploads/2026/01/09/xxx.jpg
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class UploadWebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties props;

    public UploadWebMvcConfig(StorageProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = props.getLocal().getUploadDir();
        if (!StringUtils.hasText(uploadDir)) {
            return;
        }

        // 注意：Spring 需要 "file:" 前缀
        String location = "file:" + normalizeDir(uploadDir);

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location);
    }

    private String normalizeDir(String dir) {
        // 确保以 / 或 \ 结尾，避免路径拼接问题
        if (dir.endsWith("/") || dir.endsWith("\\")) {
            return dir;
        }
        return dir + "/";
    }
}
