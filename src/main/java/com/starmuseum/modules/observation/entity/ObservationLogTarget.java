package com.starmuseum.modules.observation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observation_log_target")
public class ObservationLogTarget {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("log_id")
    private Long logId;

    @TableField("user_id")
    private Long userId;

    /**
     * CELESTIAL_BODY / TEXT
     */
    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_name")
    private String targetName;

    /**
     * DSO/STAR/PLANET/...
     */
    @TableField("body_type")
    private String bodyType;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
