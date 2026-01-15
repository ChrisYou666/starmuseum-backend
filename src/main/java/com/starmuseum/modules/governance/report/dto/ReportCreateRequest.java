package com.starmuseum.modules.governance.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReportCreateRequest {

    @NotBlank
    private String targetType; // POST|COMMENT|USER|MEDIA

    @NotNull
    private Long targetId;

    @NotBlank
    private String reasonCode; // SPAM/ABUSE/...

    private String description;

    /**
     * 0~9
     */
    private List<Long> evidenceMediaIds;
}
