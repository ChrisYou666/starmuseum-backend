package com.starmuseum.modules.governance.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorUserId;

    private String action;

    private String entityType;

    private Long entityId;

    private String detailJson;

    private LocalDateTime createdAt;
}
