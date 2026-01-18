package com.starmuseum.modules.observation.vo;

import lombok.Data;

@Data
public class ObservationTargetVO {
    private Long id;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String bodyType;
}
