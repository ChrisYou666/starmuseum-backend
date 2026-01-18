package com.starmuseum.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class CatalogStaticResourceConfig implements WebMvcConfigurer {

    private final CatalogProperties catalogProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String storageDir = catalogProperties.getStorageDir();
        if (!StringUtils.hasText(storageDir)) {
            // 没配置就不注册，避免启动报错
            return;
        }

        // URL 前缀（/catalog）
        String base = catalogProperties.effectiveResourceBaseUrl();

        // Windows 路径也 ok：Path.of("D:/xxx")
        Path root = Path.of(storageDir).toAbsolutePath().normalize();

        // Spring ResourceHandler 需要 file: 前缀 + 以 / 结尾
        String location = "file:" + root.toString().replace("\\", "/") + "/";

        // 例：/catalog/** -> D:/data/starmuseum/catalog_storage/**
        registry.addResourceHandler(base + "/**")
            .addResourceLocations(location);
    }
}
