package com.starmuseum.modules.astro.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CatalogVersionVO {

    private Long id;

    private String code;
    private String status;

    private String checksum;
    private String sourceNote;

    private LocalDateTime activatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
