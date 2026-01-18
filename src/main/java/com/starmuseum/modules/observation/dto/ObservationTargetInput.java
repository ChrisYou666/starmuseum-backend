package com.starmuseum.modules.observation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ObservationTargetInput {

    /**
     * CELESTIAL_BODY / TEXT
     */
    @NotBlank(message = "targetType 不能为空")
    private String targetType;

    /**
     * 若 targetType=CELESTIAL_BODY 可填
     */
    private Long targetId;

    @NotBlank(message = "targetName 不能为空")
    @Size(max = 128, message = "targetName 最大 128 字符")
    private String targetName;

    /**
     * DSO/STAR/PLANET/...
     */
    private String bodyType;
}
