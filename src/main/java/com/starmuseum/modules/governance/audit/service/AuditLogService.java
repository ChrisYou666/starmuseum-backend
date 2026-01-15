package com.starmuseum.modules.governance.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starmuseum.modules.governance.audit.entity.AuditLog;
import com.starmuseum.modules.governance.audit.vo.AdminAuditLogVO;

public interface AuditLogService extends IService<AuditLog> {

    /**
     * 管理端分页查询审计日志
     */
    IPage<AdminAuditLogVO> getAuditLogs(
        long page,
        long size,
        Long operatorUserId,
        String action,
        String entityType,
        Long entityId,
        String from,
        String to
    );
}
