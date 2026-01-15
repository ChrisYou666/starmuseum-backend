package com.starmuseum.modules.governance.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminTargetSummaryVO {

    private String targetType; // POST/COMMENT/USER/MEDIA
    private Long targetId;

    // 通用：作者/用户
    private Long userId;
    private String nickname;
    private String avatarUrl;

    // POST/COMMENT 用
    private Long postId;
    private String contentPreview;
    private LocalDateTime createdAt;

    // MEDIA 用
    private String originUrl;
    private String thumbUrl;
    private String mediumUrl;
}
