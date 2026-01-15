package com.starmuseum.modules.governance.report.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportVO {

    private Long id;

    private String targetType;
    private Long targetId;

    private String reasonCode;
    private String description;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 仅在详情接口返回
     */
    private List<ReportEvidenceVO> evidenceList;
}
