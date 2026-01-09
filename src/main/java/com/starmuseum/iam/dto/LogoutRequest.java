package com.starmuseum.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退出登录请求：
 * - 阶段1我们简单处理：前端把 refreshToken 带回来，后端撤销对应 session
 * - 未来可改 httpOnly cookie 方式
 */
@Data
public class LogoutRequest {

    @NotBlank
    private String refreshToken;
}
