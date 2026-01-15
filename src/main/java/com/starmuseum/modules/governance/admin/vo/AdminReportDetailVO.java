package com.starmuseum.modules.governance.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminReportDetailVO {

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

    private List<EvidenceMediaVO> evidenceList;

    private ReviewInfoVO review;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class EvidenceMediaVO {
        private Long mediaId;
        private String originUrl;
        private String thumbUrl;
        private String mediumUrl;
    }

    @Data
    public static class ReviewInfoVO {
        private Long reviewerUserId;
        private String reviewerNickname;
        private String reviewerAvatarUrl;

        private String decision; // REJECT/RESOLVE
        private String notes;

        private LocalDateTime createdAt;
    }
}
