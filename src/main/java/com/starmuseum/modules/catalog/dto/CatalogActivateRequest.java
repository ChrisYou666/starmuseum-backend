package com.starmuseum.modules.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CatalogActivateRequest {

    @NotBlank
    private String code;
}
