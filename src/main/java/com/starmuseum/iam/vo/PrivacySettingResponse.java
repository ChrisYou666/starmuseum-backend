package com.starmuseum.iam.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐私设置返回（阶段3.2）
 */
@Data
public class PrivacySettingResponse {

    private Long userId;

    private String postVisibilityDefault;

    private String defaultLocationVisibility;

    private String exactLocationPublicStrategy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
