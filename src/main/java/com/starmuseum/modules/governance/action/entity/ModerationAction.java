package com.starmuseum.modules.governance.action.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * moderation_action
 */
@Data
@TableName("moderation_action")
public class ModerationAction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String actionType;

    private Long targetUserId;

    private String targetType;

    private Long targetId;

    private Integer durationHours;

    private String reason;

    private Long operatorUserId;

    private Long relatedReportId;

    private LocalDateTime createdAt;
}
