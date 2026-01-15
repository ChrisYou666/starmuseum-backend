package com.starmuseum.modules.governance.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReportListItemVO {

    private Long id;

    private String status;
    private String targetType;
    private Long targetId;

    private String reasonCode;
    private String description;

    private Long reporterUserId;
    private String reporterNickname;
    private String reporterAvatarUrl;

    private AdminTargetSummaryVO target;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
