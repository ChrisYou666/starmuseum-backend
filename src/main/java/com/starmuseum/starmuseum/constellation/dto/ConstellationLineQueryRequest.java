package com.starmuseum.starmuseum.constellation.dto;

import lombok.Data;

@Data
public class ConstellationLineQueryRequest {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private String constellationCode;
    private String constellationName;

    private Long startBodyId;
    private Long endBodyId;
}