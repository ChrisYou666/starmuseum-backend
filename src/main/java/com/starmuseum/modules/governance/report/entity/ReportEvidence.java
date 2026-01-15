package com.starmuseum.modules.governance.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report_evidence")
public class ReportEvidence {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;

    private Long mediaId;

    private LocalDateTime createdAt;
}
