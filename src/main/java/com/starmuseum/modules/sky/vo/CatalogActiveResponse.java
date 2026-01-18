package com.starmuseum.modules.sky.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * active catalog 元信息（给前端首屏：知道当前用哪个版本、资源前缀、更新时间）
 */
@Data
public class CatalogActiveResponse {

    /**
     * active 版本号（例如 v2026_01 / v2026_02）
     * 如果你没有版本表，也至少会返回 "active"
     */
    private String activeVersion;

    /**
     * catalog storage 根目录（用于排查/展示）
     */
    private String storageDir;

    /**
     * 资源访问前缀（例如 /catalog）
     */
    private String resourceBaseUrl;

    /**
     * 最近一次激活时间（如果 DB 能查到就返回）
     */
    private LocalDateTime activatedAt;
}
