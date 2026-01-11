package com.starmuseum.modules.astro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 天体主数据（阶段2：先用亮星 STAR）
 */
@Data
@TableName("celestial_body")
public class CelestialBody {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String catalogCode;
    private String catalogVersionCode;

    private String bodyType;

    private String name;
    private String nameZh;
    private String nameEn;
    private String nameId;

    private Double raDeg;
    private Double decDeg;

    private Double mag;

    private String spectralType;
    private String constellation;
    private String wikiUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
