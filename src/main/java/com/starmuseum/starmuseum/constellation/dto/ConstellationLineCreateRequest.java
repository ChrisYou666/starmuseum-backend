package com.starmuseum.starmuseum.constellation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConstellationLineCreateRequest {

    @NotBlank(message = "constellationCode不能为空")
    private String constellationCode;

    @NotBlank(message = "constellationName不能为空")
    private String constellationName;

    @NotNull(message = "startBodyId不能为空")
    private Long startBodyId;

    @NotNull(message = "endBodyId不能为空")
    private Long endBodyId;

    /** default 0 */
    private Integer sortOrder = 0;

    private String remark;
}