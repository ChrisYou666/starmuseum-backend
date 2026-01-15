package com.starmuseum.modules.governance.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.modules.governance.report.dto.ReportCreateRequest;
import com.starmuseum.modules.governance.report.entity.Report;
import com.starmuseum.modules.governance.report.entity.ReportEvidence;
import com.starmuseum.modules.governance.report.mapper.ReportEvidenceMapper;
import com.starmuseum.modules.governance.report.mapper.ReportMapper;
import com.starmuseum.modules.governance.report.service.ReportService;
import com.starmuseum.modules.governance.report.vo.ReportEvidenceVO;
import com.starmuseum.modules.governance.report.vo.ReportVO;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.mapper.MediaMapper;
import com.starmuseum.modules.post.entity.Post;
import com.starmuseum.modules.post.entity.PostComment;
import com.starmuseum.modules.post.mapper.PostCommentMapper;
import com.starmuseum.modules.post.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private static final int MAX_EVIDENCE = 9;

    private final ReportMapper reportMapper;
    private final ReportEvidenceMapper reportEvidenceMapper;

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final UserMapper userMapper;
    private final MediaMapper mediaMapper;

    public ReportServiceImpl(ReportMapper reportMapper,
                             ReportEvidenceMapper reportEvidenceMapper,
                             PostMapper postMapper,
                             PostCommentMapper postCommentMapper,
                             UserMapper userMapper,
                             MediaMapper mediaMapper) {
        this.reportMapper = reportMapper;
        this.reportEvidenceMapper = reportEvidenceMapper;
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
        this.userMapper = userMapper;
        this.mediaMapper = mediaMapper;
    }

    @Override
    @Transactional
    public ReportVO create(ReportCreateRequest req, Long reporterUserId) {
        if (req == null) throw new IllegalArgumentException("request is null");

        String targetType = normUpper(req.getTargetType());
        Long targetId = req.getTargetId();
        String reasonCode = normUpper(req.getReasonCode());
        String description = StringUtils.hasText(req.getDescription()) ? req.getDescription().trim() : null;

        if (!isValidTargetType(targetType)) {
            throw new IllegalArgumentException("invalid targetType: " + targetType);
        }

        // 1) target 必须存在
        ensureTargetExists(targetType, targetId);

        // 2) 禁止重复 OPEN 举报（同一人对同一 target）
        Report existing = reportMapper.selectOne(new LambdaQueryWrapper<Report>()
            .eq(Report::getReporterUserId, reporterUserId)
            .eq(Report::getTargetType, targetType)
            .eq(Report::getTargetId, targetId)
            .eq(Report::getStatus, "OPEN")
            .last("LIMIT 1"));
        if (existing != null) {
            throw new IllegalArgumentException("你已对该对象发起过举报（OPEN），请勿重复提交");
        }

        // 3) evidence 校验
        List<Long> evidenceIds = req.getEvidenceMediaIds();
        List<Long> normalizedEvidenceIds = normalizeEvidenceIds(evidenceIds);

        if (normalizedEvidenceIds.size() > MAX_EVIDENCE) {
            throw new IllegalArgumentException("evidenceMediaIds 最多 " + MAX_EVIDENCE + " 张");
        }

        if (!normalizedEvidenceIds.isEmpty()) {
            List<Media> medias = mediaMapper.selectBatchIds(normalizedEvidenceIds);
            Map<Long, Media> mediaMap = new HashMap<>();
            for (Media m : medias) {
                mediaMap.put(m.getId(), m);
            }

            for (Long mid : normalizedEvidenceIds) {
                Media m = mediaMap.get(mid);
                if (m == null) {
                    throw new IllegalArgumentException("evidence media not found: " + mid);
                }
                if (!Objects.equals(m.getUserId(), reporterUserId)) {
                    throw new IllegalArgumentException("evidence media 不属于本人: " + mid);
                }
                if (!MediaBizType.REPORT_EVIDENCE.name().equalsIgnoreCase(m.getBizType())) {
                    throw new IllegalArgumentException("evidence media.bizType 必须为 REPORT_EVIDENCE: " + mid);
                }
            }
        }

        // 4) 写 report
        LocalDateTime now = LocalDateTime.now();
        Report report = new Report();
        report.setReporterUserId(reporterUserId);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReasonCode(reasonCode);
        report.setDescription(description);
        report.setStatus("OPEN");
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportMapper.insert(report);

        // 5) 写 report_evidence
        if (!normalizedEvidenceIds.isEmpty()) {
            for (Long mid : normalizedEvidenceIds) {
                ReportEvidence re = new ReportEvidence();
                re.setReportId(report.getId());
                re.setMediaId(mid);
                re.setCreatedAt(now);
                reportEvidenceMapper.insert(re);
            }
        }

        // 返回详情（含 evidence urls）
        return myDetail(report.getId(), reporterUserId);
    }

    @Override
    public IPage<ReportVO> myPage(int page, int size, Long reporterUserId, String status) {
        Page<Report> p = new Page<>(page, size);

        String statusUpper = StringUtils.hasText(status) ? status.trim().toUpperCase() : null;

        IPage<Report> ip = reportMapper.selectPage(
            p,
            new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterUserId, reporterUserId)
                .eq(StringUtils.hasText(statusUpper), Report::getStatus, statusUpper)
                .orderByDesc(Report::getId)
        );

        Page<ReportVO> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<ReportVO> list = new ArrayList<>();
        if (ip.getRecords() != null) {
            for (Report r : ip.getRecords()) {
                list.add(toVO(r, null));
            }
        }
        out.setRecords(list);
        return out;
    }

    @Override
    public ReportVO myDetail(Long reportId, Long reporterUserId) {
        Report r = reportMapper.selectById(reportId);
        if (r == null) throw new IllegalArgumentException("report not found");
        if (!Objects.equals(r.getReporterUserId(), reporterUserId)) {
            throw new IllegalArgumentException("无权查看该举报详情");
        }

        // evidence list
        List<ReportEvidence> rels = reportEvidenceMapper.selectList(
            new LambdaQueryWrapper<ReportEvidence>()
                .eq(ReportEvidence::getReportId, reportId)
                .orderByAsc(ReportEvidence::getId)
        );

        List<ReportEvidenceVO> evidenceVOList = new ArrayList<>();
        if (rels != null && !rels.isEmpty()) {
            List<Long> mediaIds = new ArrayList<>();
            for (ReportEvidence re : rels) {
                if (re.getMediaId() != null) mediaIds.add(re.getMediaId());
            }

            if (!mediaIds.isEmpty()) {
                List<Media> medias = mediaMapper.selectBatchIds(mediaIds);
                Map<Long, Media> mediaMap = new HashMap<>();
                for (Media m : medias) {
                    mediaMap.put(m.getId(), m);
                }

                for (Long mid : mediaIds) {
                    Media m = mediaMap.get(mid);
                    if (m == null) continue;
                    ReportEvidenceVO evo = new ReportEvidenceVO();
                    evo.setMediaId(m.getId());
                    evo.setOriginUrl(m.getOriginUrl());
                    evo.setThumbUrl(m.getThumbUrl());
                    evo.setMediumUrl(m.getMediumUrl());
                    evidenceVOList.add(evo);
                }
            }
        }

        return toVO(r, evidenceVOList);
    }

    @Override
    @Transactional
    public void withdraw(Long reportId, Long reporterUserId) {
        Report r = reportMapper.selectById(reportId);
        if (r == null) throw new IllegalArgumentException("report not found");
        if (!Objects.equals(r.getReporterUserId(), reporterUserId)) {
            throw new IllegalArgumentException("无权撤回该举报");
        }
        if (!"OPEN".equalsIgnoreCase(r.getStatus())) {
            throw new IllegalArgumentException("仅 OPEN 状态允许撤回");
        }

        r.setStatus("WITHDRAWN");
        r.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(r);
    }

    // =========================
    // 内部工具方法
    // =========================

    private void ensureTargetExists(String targetType, Long targetId) {
        if (targetId == null) throw new IllegalArgumentException("targetId is null");

        switch (targetType) {
            case "POST" -> {
                Post p = postMapper.selectById(targetId);
                if (p == null || p.getDeletedAt() != null) {
                    throw new IllegalArgumentException("target post not found");
                }
            }
            case "COMMENT" -> {
                PostComment c = postCommentMapper.selectById(targetId);
                if (c == null || Objects.equals(c.getDeleted(), 1)) {
                    throw new IllegalArgumentException("target comment not found");
                }
            }
            case "USER" -> {
                User u = userMapper.selectById(targetId);
                if (u == null) {
                    throw new IllegalArgumentException("target user not found");
                }
            }
            case "MEDIA" -> {
                Media m = mediaMapper.selectById(targetId);
                if (m == null) {
                    throw new IllegalArgumentException("target media not found");
                }
            }
            default -> throw new IllegalArgumentException("invalid targetType: " + targetType);
        }
    }

    private boolean isValidTargetType(String t) {
        return "POST".equals(t) || "COMMENT".equals(t) || "USER".equals(t) || "MEDIA".equals(t);
    }

    private String normUpper(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim().toUpperCase();
    }

    private List<Long> normalizeEvidenceIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) set.add(id);
        }
        return new ArrayList<>(set);
    }

    private ReportVO toVO(Report r, List<ReportEvidenceVO> evidenceList) {
        ReportVO vo = new ReportVO();
        vo.setId(r.getId());
        vo.setTargetType(r.getTargetType());
        vo.setTargetId(r.getTargetId());
        vo.setReasonCode(r.getReasonCode());
        vo.setDescription(r.getDescription());
        vo.setStatus(r.getStatus());
        vo.setCreatedAt(r.getCreatedAt());
        vo.setUpdatedAt(r.getUpdatedAt());
        vo.setEvidenceList(evidenceList);
        return vo;
    }
}
