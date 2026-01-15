package com.starmuseum.iam.dto;

import lombok.Data;

/**
 * 更新隐私设置请求（阶段3.2）
 * - 字段都允许不传：不传则不修改
 */
@Data
public class UpdatePrivacyRequest {

    /**
     * PUBLIC / PRIVATE / FOLLOWERS(预留)
     */
    private String postVisibilityDefault;

    /**
     * HIDDEN / CITY / FUZZY / EXACT
     */
    private String defaultLocationVisibility;

    /**
     * FUZZY / CITY
     */
    private String exactLocationPublicStrategy;
}
