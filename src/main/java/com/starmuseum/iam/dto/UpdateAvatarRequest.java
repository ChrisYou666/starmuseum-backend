package com.starmuseum.iam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 头像更新请求
 */
@Data
public class UpdateAvatarRequest {

    /**
     * 头像对应的 mediaId（bizType 必须是 AVATAR）
     */
    @NotNull(message = "mediaId 不能为空")
    private Long mediaId;
}
