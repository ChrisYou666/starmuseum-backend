package com.starmuseum.starmuseum.location.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 观测地点（城市/具体地标）
 */
@Data
@TableName("location")
public class Location {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String country;

    private String province;

    private String city;

    private Double latitude;

    private Double longitude;

    private String timezone;

    @TableField("altitude_m")
    private Integer altitudeM;

    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}