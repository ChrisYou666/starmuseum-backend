package com.starmuseum.modules.astro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("celestial_body")
public class CelestialBody {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("catalog_version_code")
    private String catalogVersionCode;

    @TableField("catalog_code")
    private String catalogCode;

    @TableField("body_type")
    private String bodyType;

    @TableField("name")
    private String name;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_id")
    private String nameId;

    @TableField("ra_deg")
    private Double raDeg;

    @TableField("dec_deg")
    private Double decDeg;

    @TableField("mag")
    private Double mag;

    @TableField("spectral_type")
    private String spectralType;

    @TableField("constellation")
    private String constellation;

    @TableField("wiki_url")
    private String wikiUrl;

    /**
     * Phase 5D：扩展字段（角直径、尺寸、星系类型等）
     * - 由 catalog objects 的“未知字段”自动收集后写入
     * - 前端可直接解析 JSON（MVP）
     */
    @TableField("extra_json")
    private String extraJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
