package com.starmuseum.modules.astro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("celestial_alias")
public class CelestialAlias {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bodyId;

    private String lang;
    private String aliasName;

    private LocalDateTime createdAt;
}
