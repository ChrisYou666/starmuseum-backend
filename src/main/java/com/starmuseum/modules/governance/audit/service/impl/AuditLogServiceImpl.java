package com.starmuseum.modules.governance.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.modules.governance.audit.entity.AuditLog;
import com.starmuseum.modules.governance.audit.mapper.AuditLogMapper;
import com.starmuseum.modules.governance.audit.service.AuditLogService;
import com.starmuseum.modules.governance.audit.vo.AdminAuditLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Override
    public IPage<AdminAuditLogVO> getAuditLogs(long page, long size,
                                               Long operatorUserId,
                                               String action,
                                               String entityType,
                                               Long entityId,
                                               String from,
                                               String to) {

        LocalDateTime fromDt = parseDateTime(from);
        LocalDateTime toDt = parseDateTime(to);

        LambdaQueryWrapper<AuditLog> qw = new LambdaQueryWrapper<>();
        qw.eq(operatorUserId != null, AuditLog::getOperatorUserId, operatorUserId);
        qw.eq(StringUtils.hasText(action), AuditLog::getAction, action);
        qw.eq(StringUtils.hasText(entityType), AuditLog::getEntityType, entityType);
        qw.eq(entityId != null, AuditLog::getEntityId, entityId);

        qw.ge(fromDt != null, AuditLog::getCreatedAt, fromDt);
        qw.le(toDt != null, AuditLog::getCreatedAt, toDt);

        qw.orderByDesc(AuditLog::getId);

        Page<AuditLog> p = new Page<>(page, size);
        IPage<AuditLog> ip = this.page(p, qw);

        Page<AdminAuditLogVO> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<AdminAuditLogVO> list = new ArrayList<>();
        if (ip.getRecords() != null) {
            for (AuditLog a : ip.getRecords()) {
                AdminAuditLogVO vo = new AdminAuditLogVO();
                vo.setId(a.getId());
                vo.setOperatorUserId(a.getOperatorUserId());
                vo.setAction(a.getAction());
                vo.setEntityType(a.getEntityType());
                vo.setEntityId(a.getEntityId());
                vo.setDetailJson(a.getDetailJson());
                vo.setCreatedAt(a.getCreatedAt());
                list.add(vo);
            }
        }
        out.setRecords(list);
        return out;
    }

    private LocalDateTime parseDateTime(String s) {
        if (!StringUtils.hasText(s)) return null;
        String v = s.trim();
        // 1) ISO-8601：2026-01-12T00:00:00
        try {
            return LocalDateTime.parse(v);
        } catch (Exception ignored) {
        }
        // 2) 常见格式：2026-01-12 00:00:00
        try {
            return LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        // 3) 仅日期：2026-01-12 -> 当天 00:00:00
        try {
            return LocalDateTime.parse(v + "T00:00:00");
        } catch (Exception ignored) {
        }
        return null;
    }
}
