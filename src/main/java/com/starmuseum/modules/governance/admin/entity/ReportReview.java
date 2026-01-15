package com.starmuseum.modules.governance.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report_review")
public class ReportReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;

    private Long reviewerUserId;

    /**
     * REJECT / RESOLVE；start 阶段允许为空
     */
    private String decision;

    private String notes;

    private LocalDateTime createdAt;
}
