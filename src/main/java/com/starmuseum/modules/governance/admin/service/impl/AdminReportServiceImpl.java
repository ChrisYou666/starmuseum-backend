package com.starmuseum.modules.governance.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.mapper.MediaMapper;
import com.starmuseum.modules.post.entity.Post;
import com.starmuseum.modules.post.entity.PostComment;
import com.starmuseum.modules.post.mapper.PostCommentMapper;
import com.starmuseum.modules.post.mapper.PostMapper;
import com.starmuseum.modules.governance.admin.dto.AdminReportReviewRequest;
import com.starmuseum.modules.governance.admin.entity.ReportReview;
import com.starmuseum.modules.governance.admin.mapper.ReportReviewMapper;
import com.starmuseum.modules.governance.admin.service.AdminReportService;
import com.starmuseum.modules.governance.admin.vo.AdminReportDetailVO;
import com.starmuseum.modules.governance.admin.vo.AdminReportListItemVO;
import com.starmuseum.modules.governance.admin.vo.AdminTargetSummaryVO;
import com.starmuseum.modules.governance.report.entity.Report;
import com.starmuseum.modules.governance.report.entity.ReportEvidence;
import com.starmuseum.modules.governance.report.mapper.ReportEvidenceMapper;
import com.starmuseum.modules.governance.report.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportMapper reportMapper;
    private final ReportEvidenceMapper reportEvidenceMapper;
    private final ReportReviewMapper reportReviewMapper;

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final MediaMapper mediaMapper;

    public AdminReportServiceImpl(ReportMapper reportMapper,
                                  ReportEvidenceMapper reportEvidenceMapper,
                                  ReportReviewMapper reportReviewMapper,
                                  UserMapper userMapper,
                                  PostMapper postMapper,
                                  PostCommentMapper postCommentMapper,
                                  MediaMapper mediaMapper) {
        this.reportMapper = reportMapper;
        this.reportEvidenceMapper = reportEvidenceMapper;
        this.reportReviewMapper = reportReviewMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
        this.mediaMapper = mediaMapper;
    }

    @Override
    public IPage<AdminReportListItemVO> page(int page, int size, String status, String targetType, String reasonCode) {
        Page<Report> p = new Page<>(page, size);

        LambdaQueryWrapper<Report> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Report::getId);

        if (StringUtils.hasText(status)) {
            qw.eq(Report::getStatus, status.trim().toUpperCase());
        }
        if (StringUtils.hasText(targetType)) {
            qw.eq(Report::getTargetType, targetType.trim().toUpperCase());
        }
        if (StringUtils.hasText(reasonCode)) {
            qw.eq(Report::getReasonCode, reasonCode.trim().toUpperCase());
        }

        IPage<Report> ip = reportMapper.selectPage(p, qw);
        List<Report> reports = ip.getRecords();
        if (reports == null || reports.isEmpty()) {
            Page<AdminReportListItemVO> out = new Page<>(page, size, ip.getTotal());
            out.setRecords(Collections.emptyList());
            return out;
        }

        // 批量拉 reporter 用户信息
        Set<Long> reporterIds = reports.stream().map(Report::getReporterUserId).collect(Collectors.toSet());
        Map<Long, User> reporterMap = userMapper.selectBatchIds(reporterIds).stream()
            .collect(Collectors.toMap(User::getId, x -> x, (a, b) -> a));

        List<AdminReportListItemVO> voList = new ArrayList<>();
        for (Report r : reports) {
            AdminReportListItemVO vo = new AdminReportListItemVO();
            vo.setId(r.getId());
            vo.setStatus(r.getStatus());
            vo.setTargetType(r.getTargetType());
            vo.setTargetId(r.getTargetId());
            vo.setReasonCode(r.getReasonCode());
            vo.setDescription(r.getDescription());
            vo.setCreatedAt(r.getCreatedAt());
            vo.setUpdatedAt(r.getUpdatedAt());

            User reporter = reporterMap.get(r.getReporterUserId());
            vo.setReporterUserId(r.getReporterUserId());
            if (reporter != null) {
                vo.setReporterNickname(reporter.getNickname());
                vo.setReporterAvatarUrl(reporter.getAvatarUrl());
            }

            vo.setTarget(buildTargetSummary(r.getTargetType(), r.getTargetId()));
            voList.add(vo);
        }

        Page<AdminReportListItemVO> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        out.setRecords(voList);
        return out;
    }

    @Override
    public AdminReportDetailVO detail(Long reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("reportId is null");
        }
        Report r = reportMapper.selectById(reportId);
        if (r == null) {
            throw new IllegalArgumentException("report not found: " + reportId);
        }

        AdminReportDetailVO vo = new AdminReportDetailVO();
        vo.setId(r.getId());
        vo.setStatus(r.getStatus());
        vo.setTargetType(r.getTargetType());
        vo.setTargetId(r.getTargetId());
        vo.setReasonCode(r.getReasonCode());
        vo.setDescription(r.getDescription());
        vo.setCreatedAt(r.getCreatedAt());
        vo.setUpdatedAt(r.getUpdatedAt());

        User reporter = userMapper.selectById(r.getReporterUserId());
        vo.setReporterUserId(r.getReporterUserId());
        if (reporter != null) {
            vo.setReporterNickname(reporter.getNickname());
            vo.setReporterAvatarUrl(reporter.getAvatarUrl());
        }

        vo.setTarget(buildTargetSummary(r.getTargetType(), r.getTargetId()));

        // evidence media
        List<ReportEvidence> evs = reportEvidenceMapper.selectList(new LambdaQueryWrapper<ReportEvidence>()
            .eq(ReportEvidence::getReportId, reportId)
            .orderByAsc(ReportEvidence::getId));

        if (evs == null || evs.isEmpty()) {
            vo.setEvidenceList(Collections.emptyList());
        } else {
            List<Long> mediaIds = evs.stream().map(ReportEvidence::getMediaId).filter(Objects::nonNull).toList();
            Map<Long, Media> mediaMap = mediaMapper.selectBatchIds(mediaIds).stream()
                .collect(Collectors.toMap(Media::getId, x -> x, (a, b) -> a));

            List<AdminReportDetailVO.EvidenceMediaVO> list = new ArrayList<>();
            for (Long mid : mediaIds) {
                Media m = mediaMap.get(mid);
                if (m == null) continue;

                AdminReportDetailVO.EvidenceMediaVO em = new AdminReportDetailVO.EvidenceMediaVO();
                em.setMediaId(m.getId());
                em.setOriginUrl(m.getOriginUrl());
                em.setThumbUrl(m.getThumbUrl());
                em.setMediumUrl(m.getMediumUrl());
                list.add(em);
            }
            vo.setEvidenceList(list);
        }

        // review info
        ReportReview review = reportReviewMapper.selectOne(new LambdaQueryWrapper<ReportReview>()
            .eq(ReportReview::getReportId, reportId)
            .last("LIMIT 1"));

        if (review == null) {
            vo.setReview(null);
        } else {
            AdminReportDetailVO.ReviewInfoVO rv = new AdminReportDetailVO.ReviewInfoVO();
            rv.setReviewerUserId(review.getReviewerUserId());
            rv.setDecision(review.getDecision());
            rv.setNotes(review.getNotes());
            rv.setCreatedAt(review.getCreatedAt());

            User reviewer = userMapper.selectById(review.getReviewerUserId());
            if (reviewer != null) {
                rv.setReviewerNickname(reviewer.getNickname());
                rv.setReviewerAvatarUrl(reviewer.getAvatarUrl());
            }

            vo.setReview(rv);
        }

        return vo;
    }

    @Override
    @Transactional
    public AdminReportDetailVO start(Long reportId, Long adminUserId) {
        if (reportId == null) throw new IllegalArgumentException("reportId is null");

        Report r = reportMapper.selectById(reportId);
        if (r == null) throw new IllegalArgumentException("report not found: " + reportId);

        String st = r.getStatus() == null ? null : r.getStatus().trim().toUpperCase();
        if (!"OPEN".equals(st)) {
            throw new IllegalArgumentException("只有 OPEN 状态可以 start，当前=" + r.getStatus());
        }

        // 1) 把 report 状态改为 IN_REVIEW
        r.setStatus("IN_REVIEW");
        r.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(r);

        // 2) 写入/锁定 reviewer（用 report_review 这张表存）
        ReportReview exists = reportReviewMapper.selectOne(new LambdaQueryWrapper<ReportReview>()
            .eq(ReportReview::getReportId, reportId)
            .last("LIMIT 1"));

        if (exists == null) {
            ReportReview rr = new ReportReview();
            rr.setReportId(reportId);
            rr.setReviewerUserId(adminUserId);
            rr.setDecision(null); // start 阶段允许为空
            rr.setNotes(null);
            rr.setCreatedAt(LocalDateTime.now());
            reportReviewMapper.insert(rr);
        } else {
            // 已经有人 start/占用
            if (!Objects.equals(exists.getReviewerUserId(), adminUserId)) {
                throw new IllegalArgumentException("该举报已被其他管理员 start（reviewerUserId=" + exists.getReviewerUserId() + "）");
            }
        }

        return detail(reportId);
    }

    @Override
    @Transactional
    public AdminReportDetailVO review(Long reportId, Long adminUserId, AdminReportReviewRequest req) {
        if (reportId == null) throw new IllegalArgumentException("reportId is null");
        if (req == null) throw new IllegalArgumentException("request is null");

        String decision = req.getDecision() == null ? null : req.getDecision().trim().toUpperCase();
        if (!"REJECT".equals(decision) && !"RESOLVE".equals(decision)) {
            throw new IllegalArgumentException("decision 必须为 REJECT 或 RESOLVE");
        }

        Report r = reportMapper.selectById(reportId);
        if (r == null) throw new IllegalArgumentException("report not found: " + reportId);

        String st = r.getStatus() == null ? null : r.getStatus().trim().toUpperCase();
        if (!"OPEN".equals(st) && !"IN_REVIEW".equals(st)) {
            throw new IllegalArgumentException("只有 OPEN/IN_REVIEW 状态可以 review，当前=" + r.getStatus());
        }

        // 1) 确保 report_review 存在，并且 reviewer 是本人（抢单语义）
        ReportReview rr = reportReviewMapper.selectOne(new LambdaQueryWrapper<ReportReview>()
            .eq(ReportReview::getReportId, reportId)
            .last("LIMIT 1"));

        if (rr == null) {
            rr = new ReportReview();
            rr.setReportId(reportId);
            rr.setReviewerUserId(adminUserId);
            rr.setCreatedAt(LocalDateTime.now());
            reportReviewMapper.insert(rr);
        } else {
            if (!Objects.equals(rr.getReviewerUserId(), adminUserId)) {
                throw new IllegalArgumentException("该举报已被其他管理员占用（reviewerUserId=" + rr.getReviewerUserId() + "）");
            }
            if (StringUtils.hasText(rr.getDecision())) {
                throw new IllegalArgumentException("该举报已完成 review（decision=" + rr.getDecision() + "）");
            }
        }

        rr.setDecision(decision);
        rr.setNotes(StringUtils.hasText(req.getNotes()) ? req.getNotes().trim() : null);
        reportReviewMapper.updateById(rr);

        // 2) 更新 report 状态
        if ("REJECT".equals(decision)) {
            r.setStatus("REJECTED");
        } else {
            r.setStatus("RESOLVED");
        }
        r.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(r);

        return detail(reportId);
    }

    private AdminTargetSummaryVO buildTargetSummary(String targetTypeRaw, Long targetId) {
        if (!StringUtils.hasText(targetTypeRaw) || targetId == null) {
            return null;
        }
        String targetType = targetTypeRaw.trim().toUpperCase();

        AdminTargetSummaryVO vo = new AdminTargetSummaryVO();
        vo.setTargetType(targetType);
        vo.setTargetId(targetId);

        switch (targetType) {
            case "POST" -> {
                Post post = postMapper.selectById(targetId);
                if (post == null) return vo;

                vo.setPostId(post.getId());
                vo.setUserId(post.getUserId());
                vo.setCreatedAt(post.getCreatedAt());
                vo.setContentPreview(preview(post.getContent(), 100));

                User u = userMapper.selectById(post.getUserId());
                if (u != null) {
                    vo.setNickname(u.getNickname());
                    vo.setAvatarUrl(u.getAvatarUrl());
                }
            }
            case "COMMENT" -> {
                PostComment c = postCommentMapper.selectById(targetId);
                if (c == null) return vo;

                vo.setPostId(c.getPostId());
                vo.setUserId(c.getUserId());
                vo.setCreatedAt(c.getCreatedAt());
                vo.setContentPreview(preview(c.getContent(), 100));

                User u = userMapper.selectById(c.getUserId());
                if (u != null) {
                    vo.setNickname(u.getNickname());
                    vo.setAvatarUrl(u.getAvatarUrl());
                }
            }
            case "USER" -> {
                User u = userMapper.selectById(targetId);
                if (u == null) return vo;

                vo.setUserId(u.getId());
                vo.setNickname(u.getNickname());
                vo.setAvatarUrl(u.getAvatarUrl());
            }
            case "MEDIA" -> {
                Media m = mediaMapper.selectById(targetId);
                if (m == null) return vo;

                vo.setUserId(m.getUserId());
                vo.setOriginUrl(m.getOriginUrl());
                vo.setThumbUrl(m.getThumbUrl());
                vo.setMediumUrl(m.getMediumUrl());

                User u = userMapper.selectById(m.getUserId());
                if (u != null) {
                    vo.setNickname(u.getNickname());
                    vo.setAvatarUrl(u.getAvatarUrl());
                }
            }
            default -> {
                // unknown
            }
        }

        return vo;
    }

    private String preview(String s, int max) {
        if (!StringUtils.hasText(s)) return null;
        String t = s.trim();
        if (t.length() <= max) return t;
        return t.substring(0, max);
    }
}
