package com.starmuseum.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 帖子主表
 *
 * 表：post
 */
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 正文
     */
    private String content;

    /**
     * 可见性：PUBLIC / PRIVATE / FOLLOWERS(预留)
     */
    private String visibility;

    // =========================
    // 阶段3.1：位置字段（Post 维度）
    // =========================

    /**
     * 位置可见性：HIDDEN/CITY/FUZZY/EXACT
     */
    private String locationVisibility;

    /**
     * 精确纬度（仅作者可见）
     */
    private BigDecimal locationLat;

    /**
     * 精确经度（仅作者可见）
     */
    private BigDecimal locationLon;

    /**
     * 城市名（阶段3前端传）
     */
    private String locationCity;

    /**
     * 模糊纬度（对他人展示）
     */
    private BigDecimal locationLatFuzzy;

    /**
     * 模糊经度（对他人展示）
     */
    private BigDecimal locationLonFuzzy;

    private Integer likeCount;
    private Integer commentCount;


    /**
     * 软删字段
     */
    private LocalDateTime deletedAt;
    private Long deletedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
