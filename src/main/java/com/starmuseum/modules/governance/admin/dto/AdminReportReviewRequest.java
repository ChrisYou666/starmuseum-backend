package com.starmuseum.modules.governance.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminReportReviewRequest {

    /**
     * REJECT / RESOLVE
     */
    @NotBlank(message = "decision 不能为空（REJECT/RESOLVE）")
    private String decision;

    private String notes;
}
