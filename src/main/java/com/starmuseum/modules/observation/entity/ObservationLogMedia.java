package com.starmuseum.modules.observation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observation_log_media")
public class ObservationLogMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("log_id")
    private Long logId;

    @TableField("user_id")
    private Long userId;

    @TableField("media_id")
    private Long mediaId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
