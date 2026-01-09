package com.starmuseum.iam.vo;

import lombok.Data;

/**
 * 登录/刷新返回
 */
@Data
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
