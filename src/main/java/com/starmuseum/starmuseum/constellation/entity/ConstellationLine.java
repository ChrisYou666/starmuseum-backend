package com.starmuseum.starmuseum.constellation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("constellation_line")
public class ConstellationLine implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("constellation_code")
    private String constellationCode;

    @TableField("constellation_name")
    private String constellationName;

    @TableField("start_body_id")
    private Long startBodyId;

    @TableField("end_body_id")
    private Long endBodyId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("remark")
    private String remark;

    /**
     * 逻辑删除：属性名必须是 isDeleted 才能被你现有的 MetaObjectHandler 自动填充
     * 数据库列名：is_deleted
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 时间字段：属性名必须是 createdAt/updatedAt 才能被你现有的 MetaObjectHandler 自动填充
     * 数据库列名：created_at / updated_at
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT,
            insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE,
            insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime updatedAt;
}