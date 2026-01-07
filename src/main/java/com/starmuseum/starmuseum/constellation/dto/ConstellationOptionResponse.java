package com.starmuseum.starmuseum.constellation.dto;

import lombok.Data;

@Data
public class ConstellationOptionResponse {

    /**
     * 星座代码：Ori / UMa / Lyr ...
     */
    private String constellationCode;

    /**
     * 星座名称：Orion / Ursa Major / Lyra ...
     */
    private String constellationName;

    /**
     * 该星座当前库里有多少条线段（constellation_line 行数）
     */
    private Long lineCount;
}
