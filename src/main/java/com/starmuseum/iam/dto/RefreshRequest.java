package com.starmuseum.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 token 请求
 */
@Data
public class RefreshRequest {

    @NotBlank
    private String refreshToken;

}
