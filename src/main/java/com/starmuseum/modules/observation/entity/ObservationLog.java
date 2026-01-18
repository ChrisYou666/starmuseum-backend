package com.starmuseum.modules.observation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observation_log")
public class ObservationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("observed_at")
    private LocalDateTime observedAt;

    /**
     * PHOTO / VISUAL / OTHER
     */
    @TableField("method")
    private String method;

    @TableField("device_profile_id")
    private Long deviceProfileId;

    @TableField("notes")
    private String notes;

    /**
     * 1=成功 0=失败
     */
    @TableField("success")
    private Integer success;

    /**
     * 1~5
     */
    @TableField("rating")
    private Integer rating;

    /**
     * EXACT / FUZZY / HIDDEN
     */
    @TableField("location_visibility")
    private String locationVisibility;

    @TableField("location_lat")
    private Double locationLat;

    @TableField("location_lon")
    private Double locationLon;

    @TableField("location_lat_fuzzy")
    private Double locationLatFuzzy;

    @TableField("location_lon_fuzzy")
    private Double locationLonFuzzy;

    @TableField("location_city")
    private String locationCity;

    @TableField("published")
    private Integer published;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
