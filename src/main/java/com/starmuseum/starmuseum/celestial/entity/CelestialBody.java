package com.starmuseum.starmuseum.celestial.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.starmuseum.starmuseum.celestial.enums.CelestialType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_celestial_body")
public class CelestialBody {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称（英文/通用名） */
    private String name;

    /** 别名/中文名（可空） */
    private String alias;

    /** 类型 */
    private CelestialType type;

    /** 所属星座（可空） */
    private String constellation;

    /** 赤经 RA（小时制，0~24；可空） */
    @TableField("ra_hours")
    private Double raHours;

    /** 赤纬 Dec（度，-90~90；可空） */
    @TableField("dec_degrees")
    private Double decDegrees;

    /** 视星等（越小越亮；可空） */
    private Double magnitude;

    /** 距离（光年；可空） */
    @TableField("distance_ly")
    private Double distanceLy;

    /** 光谱型（如 G2V；可空） */
    @TableField("spectral_type")
    private String spectralType;

    /** 表面温度（K；可空） */
    @TableField("temperature_k")
    private Integer temperatureK;

    /** 简介/描述（可空） */
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}