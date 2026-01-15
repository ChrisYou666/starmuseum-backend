package com.starmuseum.modules.governance.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterUserId;

    /**
     * POST / COMMENT / USER / MEDIA
     */
    private String targetType;

    private Long targetId;

    /**
     * SPAM / ABUSE / NUDITY / VIOLENCE / OTHERS ...
     */
    private String reasonCode;

    private String description;

    /**
     * OPEN / IN_REVIEW / RESOLVED / REJECTED / WITHDRAWN
     */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
