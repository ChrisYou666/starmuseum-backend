package com.starmuseum.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建帖子请求
 */
@Data
public class PostCreateRequest {

    @NotBlank
    private String content;

    /**
     * 可见性：PUBLIC / PRIVATE / FOLLOWERS(预留)
     * 允许为空：为空时用 user_privacy_setting.post_visibility_default
     */
    private String visibility;

    /**
     * 图片 mediaId 列表（顺序即 sortNo）
     */
    private List<Long> mediaIds;

    // =========================
    // 阶段3.1：位置字段（可选）
    // =========================

    /**
     * 纬度（可选）
     */
    private Double lat;

    /**
     * 经度（可选）
     */
    private Double lon;

    /**
     * 城市名（阶段3先由前端传）
     */
    private String cityName;

    /**
     * 位置可见性：HIDDEN/CITY/FUZZY/EXACT
     */
    private String locationVisibility;
}
