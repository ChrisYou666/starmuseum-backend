package com.starmuseum.modules.astro.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("catalog_version")
public class CatalogVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String status;

    private String checksum;
    private String sourceNote;

    private LocalDateTime activatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
