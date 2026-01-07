package com.starmuseum.starmuseum.constellation.vo;

import lombok.Data;

@Data
public class ConstellationLineSegment {
    private Long id;
    private String constellationCode;
    private String constellationName;
    private Long startBodyId;
    private Long endBodyId;
    private Integer sortOrder;
}