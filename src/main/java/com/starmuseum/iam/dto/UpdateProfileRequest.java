package com.starmuseum.iam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {

    @NotBlank
    @Size(min = 1, max = 64)
    private String nickname;

    @Size(max = 512)
    private String avatarUrl;

    @Size(max = 512)
    private String bio;

}
