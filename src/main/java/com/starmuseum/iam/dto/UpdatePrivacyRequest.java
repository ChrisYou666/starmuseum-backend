package com.starmuseum.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新隐私设置请求
 */
@Data
public class UpdatePrivacyRequest {

    /**
     * PUBLIC / PRIVATE / FOLLOWERS(预留)
     */
    @NotBlank
    private String postVisibilityDefault;

}
