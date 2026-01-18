package com.starmuseum.modules.catalog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端/公开端展示用的 catalog_version 视图（避免直接暴露表全部字段）
 */
@Data
public class CatalogVersionVO {
    private String code;
    private String schemaVersion;
    private String status;

    private String manifestChecksum;
    private LocalDateTime buildTime;

    private LocalDateTime importedAt;
    private LocalDateTime validatedAt;
    private LocalDateTime activatedAt;

    private String lastError;
}
