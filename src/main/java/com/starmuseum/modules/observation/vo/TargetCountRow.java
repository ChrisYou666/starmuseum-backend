package com.starmuseum.modules.observation.vo;

import lombok.Data;

@Data
public class TargetCountRow {

    private Long targetId;

    private String targetName;

    private String bodyType;

    private Long cnt;
}
