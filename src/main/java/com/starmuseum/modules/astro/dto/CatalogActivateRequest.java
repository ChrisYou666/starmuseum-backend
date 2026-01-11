package com.starmuseum.modules.astro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CatalogActivateRequest {

    /**
     * 要激活的 catalog 版本 code，例如：v2026_01
     */
    @NotBlank(message = "code 不能为空")
    private String code;
}
