package com.starmuseum.modules.observation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observation_log_post_link")
public class ObservationLogPostLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("log_id")
    private Long logId;

    @TableField("user_id")
    private Long userId;

    @TableField("post_id")
    private Long postId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
