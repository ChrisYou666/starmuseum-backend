package com.starmuseum.modules.catalog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CatalogValidateResponse {
    private String code;
    private String status;
    private LocalDateTime validatedAt;

    /**
     * 若校验失败，issues 会列出原因（给管理端/脚本显示）
     */
    private List<String> issues = new ArrayList<>();
}
