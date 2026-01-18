package com.starmuseum.modules.observation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.enums.LocationVisibility;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.common.util.GeoFuzzUtil;
import com.starmuseum.modules.observation.dto.ObservationPublishRequest;
import com.starmuseum.modules.observation.entity.ObservationLog;
import com.starmuseum.modules.observation.entity.ObservationLogMedia;
import com.starmuseum.modules.observation.entity.ObservationLogPostLink;
import com.starmuseum.modules.observation.entity.ObservationLogTarget;
import com.starmuseum.modules.observation.mapper.ObservationLogMapper;
import com.starmuseum.modules.observation.mapper.ObservationLogMediaMapper;
import com.starmuseum.modules.observation.mapper.ObservationLogPostLinkMapper;
import com.starmuseum.modules.observation.mapper.ObservationLogTargetMapper;
import com.starmuseum.modules.observation.service.ObservationPublishService;
import com.starmuseum.modules.observation.vo.ObservationPublishResponse;
import com.starmuseum.modules.post.dto.PostCreateRequest;
import com.starmuseum.modules.post.service.PostService;
import com.starmuseum.modules.post.vo.PostDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObservationPublishServiceImpl implements ObservationPublishService {

    private final ObservationLogMapper logMapper;
    private final ObservationLogTargetMapper targetMapper;
    private final ObservationLogMediaMapper mediaMapper;
    private final ObservationLogPostLinkMapper linkMapper;
    private final PostService postService;

    public ObservationPublishServiceImpl(ObservationLogMapper logMapper,
                                         ObservationLogTargetMapper targetMapper,
                                         ObservationLogMediaMapper mediaMapper,
                                         ObservationLogPostLinkMapper linkMapper,
                                         PostService postService) {
        this.logMapper = logMapper;
        this.targetMapper = targetMapper;
        this.mediaMapper = mediaMapper;
        this.linkMapper = linkMapper;
        this.postService = postService;
    }

    @Override
    @Transactional
    public ObservationPublishResponse publishMy(Long userId, Long logId, ObservationPublishRequest req) {
        if (userId == null) throw new BizException(401, "未登录");
        if (logId == null) throw new BizException(400, "logId 不能为空");
        if (req == null) req = new ObservationPublishRequest();

        ObservationLog log = logMapper.selectById(logId);
        if (log == null || log.getDeletedAt() != null) throw new BizException(40400, "观测日志不存在");
        if (!Objects.equals(log.getUserId(), userId)) throw new BizException(403, "无权限");

        // 幂等：已发布则直接返回 link
        ObservationLogPostLink existing = linkMapper.selectOne(new LambdaQueryWrapper<ObservationLogPostLink>()
            .eq(ObservationLogPostLink::getLogId, logId)
            .eq(ObservationLogPostLink::getUserId, userId)
            .last("LIMIT 1"));

        if (existing != null) {
            if (!Objects.equals(log.getPublished(), 1)) {
                log.setPublished(1);
                log.setUpdatedAt(LocalDateTime.now());
                logMapper.updateById(log);
            }
            ObservationPublishResponse r = new ObservationPublishResponse();
            r.setLogId(logId);
            r.setPostId(existing.getPostId());
            r.setAlreadyPublished(true);
            return r;
        }

        List<ObservationLogTarget> targets = targetMapper.selectList(new LambdaQueryWrapper<ObservationLogTarget>()
            .eq(ObservationLogTarget::getLogId, logId)
            .eq(ObservationLogTarget::getUserId, userId)
            .orderByAsc(ObservationLogTarget::getId));

        List<ObservationLogMedia> mediaList = mediaMapper.selectList(new LambdaQueryWrapper<ObservationLogMedia>()
            .eq(ObservationLogMedia::getLogId, logId)
            .eq(ObservationLogMedia::getUserId, userId)
            .orderByAsc(ObservationLogMedia::getSortOrder)
            .orderByAsc(ObservationLogMedia::getId));

        List<Long> mediaIds = mediaList.stream()
            .map(ObservationLogMedia::getMediaId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 1) 拼 content（MVP：目标标签写进 content）
        String content = buildContent(log, targets, req);

        // 2) 组装 PostCreateRequest（完全对齐你的 DTO）
        PostCreateRequest postReq = new PostCreateRequest();
        postReq.setContent(content);

        // visibility 允许为空：为空时用 user_privacy_setting.post_visibility_default（你代码注释写的）
        if (req.getVisibility() != null && !req.getVisibility().trim().isEmpty()) {
            postReq.setVisibility(req.getVisibility().trim());
        } else {
            postReq.setVisibility(null);
        }

        postReq.setMediaIds(mediaIds);

        // 3) 可选：把日志的位置信息带到帖子（更符合阶段3治理链路）
        if (Boolean.TRUE.equals(req.getIncludeLocation())) {
            applyPostLocation(postReq, log);
        }

        // 4) 创建帖子（注意你真实签名：create(req, currentUserId)）
        PostDetailResponse created = postService.create(postReq, userId);
        if (created == null || created.getId() == null) throw new BizException(500, "发布失败：post 创建异常");

        Long postId = created.getId();

        // 5) 写 link
        LocalDateTime now = LocalDateTime.now();
        ObservationLogPostLink link = new ObservationLogPostLink();
        link.setLogId(logId);
        link.setUserId(userId);
        link.setPostId(postId);
        link.setCreatedAt(now);
        link.setUpdatedAt(now);
        linkMapper.insert(link);

        // 6) 更新 log.published
        log.setPublished(1);
        log.setUpdatedAt(now);
        logMapper.updateById(log);

        ObservationPublishResponse resp = new ObservationPublishResponse();
        resp.setLogId(logId);
        resp.setPostId(postId);
        resp.setAlreadyPublished(false);
        return resp;
    }

    private void applyPostLocation(PostCreateRequest postReq, ObservationLog log) {
        LocationVisibility lv = GeoFuzzUtil.parseVisibility(log.getLocationVisibility());

        // CITY/HIDDEN：不传坐标（CITY 只传 cityName）
        if (lv == LocationVisibility.HIDDEN) return;

        postReq.setCityName(log.getLocationCity());
        postReq.setLocationVisibility(lv.name());

        if (lv == LocationVisibility.CITY) {
            postReq.setLat(null);
            postReq.setLon(null);
            return;
        }

        // EXACT/FUZZY：传精确坐标（由 PostService 内部按 visibility 执行阶段3治理）
        if (log.getLocationLat() != null && log.getLocationLon() != null) {
            postReq.setLat(log.getLocationLat());
            postReq.setLon(log.getLocationLon());
        } else if (log.getLocationLatFuzzy() != null && log.getLocationLonFuzzy() != null) {
            // 兜底：如果 exact 没存，至少用 fuzzy
            postReq.setLat(log.getLocationLatFuzzy());
            postReq.setLon(log.getLocationLonFuzzy());
        }
    }

    private String buildContent(ObservationLog log, List<ObservationLogTarget> targets, ObservationPublishRequest req) {
        StringBuilder sb = new StringBuilder();

        if (req.getExtraText() != null && !req.getExtraText().trim().isEmpty()) {
            sb.append(req.getExtraText().trim()).append("\n\n");
        }

        sb.append("【观测日志】");

        if (log.getObservedAt() != null) {
            sb.append(" ").append(log.getObservedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        sb.append("\n方式：").append(log.getMethod());

        if (targets != null && !targets.isEmpty()) {
            sb.append("\n目标：");
            for (int i = 0; i < targets.size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(targets.get(i).getTargetName());
            }
        }

        if (log.getNotes() != null && !log.getNotes().trim().isEmpty()) {
            sb.append("\n记录：").append(log.getNotes().trim());
        }

        if (log.getSuccess() != null) {
            sb.append("\n结果：").append(Objects.equals(log.getSuccess(), 1) ? "成功" : "失败");
        }
        if (log.getRating() != null) {
            sb.append("\n评分：").append(log.getRating()).append("/5");
        }

        // 目标标签（MVP：文本标签）
        if (targets != null && !targets.isEmpty()) {
            sb.append("\n\n#观测 ");
            for (ObservationLogTarget tg : targets) {
                String tag = tg.getTargetName();
                if (tag == null) continue;
                tag = tag.trim();
                if (tag.isEmpty()) continue;
                sb.append("#").append(tag.replace(" ", "_")).append(" ");
            }
        }

        return sb.toString().trim();
    }
}
