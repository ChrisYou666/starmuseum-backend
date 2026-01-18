package com.starmuseum.modules.observation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ObservationPublishRequest {

    /**
     * 发帖可见性：PUBLIC / FOLLOWERS / PRIVATE（按你现有 post.visibility 约定）
     * 为空则默认 PUBLIC
     */
    private String visibility;

    /**
     * 是否在动态内容里包含位置信息（不改 post 表，MVP 写入 content 文本）
     */
    private Boolean includeLocation;

    /**
     * 可选：附加一句发布文案（会拼到 content 顶部）
     */
    @Size(max = 300, message = "extraText 最大 300 字符")
    private String extraText;
}
