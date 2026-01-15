package com.starmuseum.modules.governance.admin.dto;

import lombok.Data;

@Data
public class AdminReportQueryRequest {
    private String status;     // OPEN/IN_REVIEW/RESOLVED/REJECTED/WITHDRAWN
    private String targetType; // POST/COMMENT/USER/MEDIA
    private String reasonCode; // SPAM/ABUSE/...
}
