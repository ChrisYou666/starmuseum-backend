package com.starmuseum.modules.governance.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAuditLogVO {

    private Long id;
    private Long operatorUserId;
    private String action;
    private String entityType;
    private Long entityId;
    private String detailJson;
    private LocalDateTime createdAt;
}
