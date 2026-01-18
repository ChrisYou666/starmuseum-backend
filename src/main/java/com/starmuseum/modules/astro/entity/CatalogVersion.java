package com.starmuseum.modules.astro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * catalog_version（阶段2已存在，阶段4.1 增强：manifest/导入/校验/回滚等字段）
 *
 * 兼容策略：
 * - 保留你阶段2/3字段（code/status/checksum/sourceNote/activatedAt/createdAt/updatedAt）
 * - 阶段4.1 新增字段通过 Flyway V12 增加（schemaVersion/manifestJson/manifestChecksum/importedAt/validatedAt/...）
 */
@Data
@TableName("catalog_version")
public class CatalogVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 版本号，例如 v2026_01
     */
    private String code;

    /**
     * 数据结构版本（给未来 schema 变更预留）
     */
    private String schemaVersion;

    /**
     * 状态：
     * - IMPORTED / VALIDATED / ACTIVE / INACTIVE / FAILED
     *
     * 说明：
     * - 线上查询永远只读“active 指针”（sys_kv_config.active_catalog_version），status 主要用于管理端展示与兜底。
     */
    private String status;

    /**
     * 兼容字段：阶段2 demo 用的 checksum（你可以继续用它存 manifestChecksum 或整个包的 checksum）
     */
    private String checksum;

    /**
     * 兼容字段：阶段2 demo 用的说明
     */
    private String sourceNote;

    /**
     * 新增：manifest.json 原文（JSON）
     */
    private String manifestJson;

    /**
     * 新增：manifest.json 的 sha256（或 checksums 中 manifest 的 hash）
     */
    private String manifestChecksum;

    /**
     * 新增：manifest.buildTime（可选）
     */
    private LocalDateTime buildTime;

    /**
     * 新增：导入人（userId）
     */
    private Long importedBy;

    /**
     * 新增：导入时间（导入完成写入）
     */
    private LocalDateTime importedAt;

    /**
     * 新增：校验通过时间
     */
    private LocalDateTime validatedAt;

    /**
     * 激活时间
     */
    private LocalDateTime activatedAt;

    /**
     * 新增：失败原因（导入/校验失败）
     */
    private String lastError;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
