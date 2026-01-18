package com.starmuseum.modules.catalog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CatalogImportResponse {
    private String code;
    private String status;
    private LocalDateTime importedAt;
    private String message;
}
