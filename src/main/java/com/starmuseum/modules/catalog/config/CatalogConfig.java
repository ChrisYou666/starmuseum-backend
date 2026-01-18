package com.starmuseum.modules.catalog.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CatalogStorageProperties.class)
public class CatalogConfig {
}
