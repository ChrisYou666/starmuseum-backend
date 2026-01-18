package com.starmuseum.modules.observation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.common.enums.LocationVisibility;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.common.util.GeoFuzzUtil;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.mapper.MediaMapper;
import com.starmuseum.modules.observation.dto.ObservationLogCreateRequest;
import com.starmuseum.modules.observation.dto.ObservationLogUpdateRequest;
import com.starmuseum.modules.observation.dto.ObservationTargetInput;
import com.starmuseum.modules.observation.entity.ObservationLog;
import com.starmuseum.modules.observation.entity.ObservationLogMedia;
import com.starmuseum.modules.observation.entity.ObservationLogPostLink;
import com.starmuseum.modules.observation.entity.ObservationLogTarget;
import com.starmuseum.modules.observation.enums.ObservationMethod;
import com.starmuseum.modules.observation.enums.ObservationTargetType;
import com.starmuseum.modules.observation.mapper.ObservationLogMapper;
import com.starmuseum.modules.observation.mapper.ObservationLogMediaMapper;
import com.starmuseum.modules.observation.mapper.ObservationLogPostLinkMapper;
import com.starmuseum.modules.observation.mapper.ObservationLogTargetMapper;
import com.starmuseum.modules.observation.service.ObservationLogService;
import com.starmuseum.modules.observation.vo.ObservationLogDetailVO;
import com.starmuseum.modules.observation.vo.ObservationLogVO;
import com.starmuseum.modules.observation.vo.ObservationMediaVO;
import com.starmuseum.modules.observation.vo.ObservationTargetVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObservationLogServiceImpl implements ObservationLogService {

    /**
     * FUZZY 降精度：你可以按阶段3策略调整
     * - 2 位小数：约 1.1km
     * - 3 位小数：约 110m
     */
    private static final int FUZZ_DECIMALS = 2;

    private final ObservationLogMapper logMapper;
    private final ObservationLogTargetMapper targetMapper;
    private final ObservationLogMediaMapper mediaMapper;
    private final ObservationLogPostLinkMapper linkMapper;
    private final MediaMapper mediaBaseMapper;

    public ObservationLogServiceImpl(ObservationLogMapper logMapper,
                                     ObservationLogTargetMapper targetMapper,
                                     ObservationLogMediaMapper mediaMapper,
                                     ObservationLogPostLinkMapper linkMapper,
                                     MediaMapper mediaBaseMapper) {
        this.logMapper = logMapper;
        this.targetMapper = targetMapper;
        this.mediaMapper = mediaMapper;
        this.linkMapper = linkMapper;
        this.mediaBaseMapper = mediaBaseMapper;
    }

    @Override
    @Transactional
    public ObservationLogDetailVO createMy(Long userId, ObservationLogCreateRequest req) {
        if (userId == null) throw new BizException(401, "未登录");
        if (req == null) throw new BizException(400, "body 不能为空");

        ObservationMethod method = parseMethod(req.getMethod());
        LocationVisibility lv = GeoFuzzUtil.parseVisibility(req.getLocationVisibility());

        validateLocation(lv, req.getLat(), req.getLon());

        LocalDateTime now = LocalDateTime.now();

        ObservationLog log = new ObservationLog();
        log.setUserId(userId);
        log.setObservedAt(req.getObservedAt());
        log.setMethod(method.name());
        log.setDeviceProfileId(req.getDeviceProfileId());
        log.setNotes(trimOrNull(req.getNotes()));
        log.setSuccess(req.getSuccess());
        log.setRating(req.getRating());

        log.setLocationVisibility(lv.name());
        log.setLocationCity(trimOrNull(req.getCityName()));
        applyLocationFields(log, lv, req.getLat(), req.getLon());

        log.setPublished(0);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        log.setDeletedAt(null);

        logMapper.insert(log);

        saveTargets(userId, log.getId(), req.getTargets(), now);
        saveMedia(userId, log.getId(), req.getMediaIds(), now);

        return getMy(userId, log.getId());
    }

    @Override
    @Transactional
    public ObservationLogDetailVO updateMy(Long userId, Long logId, ObservationLogUpdateRequest req) {
        if (userId == null) throw new BizException(401, "未登录");
        if (logId == null) throw new BizException(400, "logId 不能为空");
        if (req == null) throw new BizException(400, "body 不能为空");

        ObservationLog log = getOwnedLogOrThrow(userId, logId);

        // method 默认不允许改（保持一致性）
        if (req.getMethod() != null && !req.getMethod().trim().isEmpty()) {
            String newMethod = parseMethod(req.getMethod()).name();
            if (!Objects.equals(log.getMethod(), newMethod)) {
                throw new BizException(400, "method 不允许修改（如确需修改请调整后端规则）");
            }
        }

        if (req.getObservedAt() != null) log.setObservedAt(req.getObservedAt());
        if (req.getDeviceProfileId() != null) log.setDeviceProfileId(req.getDeviceProfileId());
        if (req.getNotes() != null) log.setNotes(trimOrNull(req.getNotes()));
        if (req.getSuccess() != null) log.setSuccess(req.getSuccess());
        if (req.getRating() != null) log.setRating(req.getRating());
        if (req.getCityName() != null) log.setLocationCity(trimOrNull(req.getCityName()));

        // location
        if (req.getLocationVisibility() != null && !req.getLocationVisibility().trim().isEmpty()) {
            LocationVisibility lv = GeoFuzzUtil.parseVisibility(req.getLocationVisibility());
            Double lat = req.getLat() != null ? req.getLat() : log.getLocationLat();
            Double lon = req.getLon() != null ? req.getLon() : log.getLocationLon();
            validateLocation(lv, lat, lon);

            log.setLocationVisibility(lv.name());
            applyLocationFields(log, lv, lat, lon);
        } else {
            // 未改 visibility，但改 lat/lon
            if (req.getLat() != null || req.getLon() != null) {
                LocationVisibility lv = GeoFuzzUtil.parseVisibility(log.getLocationVisibility());
                Double lat = req.getLat() != null ? req.getLat() : log.getLocationLat();
                Double lon = req.getLon() != null ? req.getLon() : log.getLocationLon();
                validateLocation(lv, lat, lon);
                applyLocationFields(log, lv, lat, lon);
            }
        }

        log.setUpdatedAt(LocalDateTime.now());
        logMapper.updateById(log);

        // targets（传入则全量覆盖）
        if (req.getTargets() != null) {
            targetMapper.delete(new LambdaQueryWrapper<ObservationLogTarget>()
                .eq(ObservationLogTarget::getLogId, logId)
                .eq(ObservationLogTarget::getUserId, userId));
            saveTargets(userId, logId, req.getTargets(), LocalDateTime.now());
        }

        // media（传入则全量覆盖）
        if (req.getMediaIds() != null) {
            mediaMapper.delete(new LambdaQueryWrapper<ObservationLogMedia>()
                .eq(ObservationLogMedia::getLogId, logId)
                .eq(ObservationLogMedia::getUserId, userId));
            saveMedia(userId, logId, req.getMediaIds(), LocalDateTime.now());
        }

        return getMy(userId, logId);
    }

    @Override
    public IPage<ObservationLogVO> pageMy(Long userId, int page, int size) {
        if (userId == null) throw new BizException(401, "未登录");
        if (page <= 0) page = 1;
        if (size <= 0) size = 10;
        if (size > 50) size = 50;

        Page<ObservationLog> mpPage = new Page<>(page, size);

        LambdaQueryWrapper<ObservationLog> qw = new LambdaQueryWrapper<ObservationLog>()
            .eq(ObservationLog::getUserId, userId)
            .isNull(ObservationLog::getDeletedAt)
            .orderByDesc(ObservationLog::getObservedAt)
            .orderByDesc(ObservationLog::getId);

        IPage<ObservationLog> res = logMapper.selectPage(mpPage, qw);

        Map<Long, Long> logIdToPostId = fetchLogPostLinks(userId, res.getRecords());

        return res.convert(e -> {
            ObservationLogVO vo = new ObservationLogVO();
            vo.setId(e.getId());
            vo.setObservedAt(e.getObservedAt());
            vo.setMethod(e.getMethod());
            vo.setDeviceProfileId(e.getDeviceProfileId());
            vo.setSuccess(e.getSuccess());
            vo.setRating(e.getRating());
            vo.setLocationVisibility(e.getLocationVisibility());
            vo.setLocationCity(e.getLocationCity());
            vo.setPublished(e.getPublished());
            vo.setPostId(logIdToPostId.get(e.getId()));
            vo.setCreatedAt(e.getCreatedAt());
            vo.setUpdatedAt(e.getUpdatedAt());
            return vo;
        });
    }

    @Override
    public ObservationLogDetailVO getMy(Long userId, Long logId) {
        if (userId == null) throw new BizException(401, "未登录");
        if (logId == null) throw new BizException(400, "logId 不能为空");

        ObservationLog log = getOwnedLogOrThrow(userId, logId);

        ObservationLogDetailVO vo = new ObservationLogDetailVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setObservedAt(log.getObservedAt());
        vo.setMethod(log.getMethod());
        vo.setDeviceProfileId(log.getDeviceProfileId());
        vo.setNotes(log.getNotes());
        vo.setSuccess(log.getSuccess());
        vo.setRating(log.getRating());

        vo.setLocationVisibility(log.getLocationVisibility());
        vo.setLocationLat(log.getLocationLat());
        vo.setLocationLon(log.getLocationLon());
        vo.setLocationLatFuzzy(log.getLocationLatFuzzy());
        vo.setLocationLonFuzzy(log.getLocationLonFuzzy());
        vo.setLocationCity(log.getLocationCity());

        vo.setPublished(log.getPublished());

        ObservationLogPostLink link = linkMapper.selectOne(new LambdaQueryWrapper<ObservationLogPostLink>()
            .eq(ObservationLogPostLink::getLogId, logId)
            .eq(ObservationLogPostLink::getUserId, userId)
            .last("LIMIT 1"));
        if (link != null) vo.setPostId(link.getPostId());

        vo.setCreatedAt(log.getCreatedAt());
        vo.setUpdatedAt(log.getUpdatedAt());

        List<ObservationLogTarget> targets = targetMapper.selectList(new LambdaQueryWrapper<ObservationLogTarget>()
            .eq(ObservationLogTarget::getLogId, logId)
            .eq(ObservationLogTarget::getUserId, userId)
            .orderByAsc(ObservationLogTarget::getId));

        vo.setTargets(targets.stream().map(t -> {
            ObservationTargetVO tv = new ObservationTargetVO();
            tv.setId(t.getId());
            tv.setTargetType(t.getTargetType());
            tv.setTargetId(t.getTargetId());
            tv.setTargetName(t.getTargetName());
            tv.setBodyType(t.getBodyType());
            return tv;
        }).collect(Collectors.toList()));

        List<ObservationLogMedia> logMedia = mediaMapper.selectList(new LambdaQueryWrapper<ObservationLogMedia>()
            .eq(ObservationLogMedia::getLogId, logId)
            .eq(ObservationLogMedia::getUserId, userId)
            .orderByAsc(ObservationLogMedia::getSortOrder)
            .orderByAsc(ObservationLogMedia::getId));

        vo.setMediaList(buildMediaVO(logMedia));

        return vo;
    }

    @Override
    @Transactional
    public void deleteMy(Long userId, Long logId) {
        if (userId == null) throw new BizException(401, "未登录");
        if (logId == null) throw new BizException(400, "logId 不能为空");

        ObservationLog log = getOwnedLogOrThrow(userId, logId);
        if (log.getDeletedAt() != null) return;

        log.setDeletedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        logMapper.updateById(log);
    }

    // ===== internal helpers =====

    private ObservationLog getOwnedLogOrThrow(Long userId, Long logId) {
        ObservationLog log = logMapper.selectById(logId);
        if (log == null || log.getDeletedAt() != null) {
            throw new BizException(40400, "观测日志不存在");
        }
        if (!Objects.equals(log.getUserId(), userId)) {
            throw new BizException(403, "无权限");
        }
        return log;
    }

    private ObservationMethod parseMethod(String v) {
        ObservationMethod m = ObservationMethod.fromString(v);
        if (m == null) throw new BizException(400, "method 必须为 PHOTO/VISUAL/OTHER");
        return m;
    }

    private void validateLocation(LocationVisibility lv, Double lat, Double lon) {
        // HIDDEN/CITY 不强制 lat/lon
        if (lv == LocationVisibility.HIDDEN || lv == LocationVisibility.CITY) return;

        // EXACT/FUZZY 需要坐标
        if (lat == null || lon == null) throw new BizException(400, "EXACT/FUZZY 需要提供 lat/lon");
        try {
            GeoFuzzUtil.validateLatLon(lat, lon);
        } catch (IllegalArgumentException e) {
            throw new BizException(400, e.getMessage());
        }
    }

    private void applyLocationFields(ObservationLog log, LocationVisibility lv, Double lat, Double lon) {
        if (lv == LocationVisibility.HIDDEN || lv == LocationVisibility.CITY) {
            log.setLocationLat(null);
            log.setLocationLon(null);
            log.setLocationLatFuzzy(null);
            log.setLocationLonFuzzy(null);
            return;
        }

        log.setLocationLat(lat);
        log.setLocationLon(lon);

        if (lv == LocationVisibility.FUZZY) {
            // 使用你现有 GeoFuzzUtil.fuzz(value, decimals)
            double latF = GeoFuzzUtil.fuzz(lat, FUZZ_DECIMALS);
            double lonF = GeoFuzzUtil.fuzz(lon, FUZZ_DECIMALS);
            log.setLocationLatFuzzy(latF);
            log.setLocationLonFuzzy(lonF);
        } else {
            // EXACT：fuzzy 字段写 exact，便于前端统一取值
            log.setLocationLatFuzzy(lat);
            log.setLocationLonFuzzy(lon);
        }
    }

    private void saveTargets(Long userId, Long logId, List<ObservationTargetInput> targets, LocalDateTime now) {
        if (targets == null || targets.isEmpty()) return;

        for (ObservationTargetInput in : targets) {
            ObservationTargetType t = ObservationTargetType.fromString(in.getTargetType());
            if (t == null) throw new BizException(400, "targetType 必须为 CELESTIAL_BODY/TEXT");

            String name = trimOrNull(in.getTargetName());
            if (name == null || name.isEmpty()) throw new BizException(400, "targetName 不能为空");

            ObservationLogTarget e = new ObservationLogTarget();
            e.setUserId(userId);
            e.setLogId(logId);
            e.setTargetType(t.name());
            e.setTargetId(in.getTargetId());
            e.setTargetName(name);
            e.setBodyType(trimOrNull(in.getBodyType()));
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            targetMapper.insert(e);
        }
    }

    private void saveMedia(Long userId, Long logId, List<Long> mediaIds, LocalDateTime now) {
        if (mediaIds == null || mediaIds.isEmpty()) return;

        int sort = 0;
        for (Long mid : mediaIds) {
            if (mid == null) continue;

            Media m = mediaBaseMapper.selectById(mid);
            if (m == null) throw new BizException(40400, "media 不存在: " + mid);
            if (!Objects.equals(m.getUserId(), userId)) throw new BizException(403, "media 不属于当前用户: " + mid);

            // 可选：限制只能复用 POST 类型的 media（如果你们上传就是 POST）
            // if (!"POST".equalsIgnoreCase(m.getBizType())) throw new BizException(400, "media.bizType 必须为 POST: " + mid);

            ObservationLogMedia e = new ObservationLogMedia();
            e.setUserId(userId);
            e.setLogId(logId);
            e.setMediaId(mid);
            e.setSortOrder(sort++);
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            mediaMapper.insert(e);
        }
    }

    private Map<Long, Long> fetchLogPostLinks(Long userId, List<ObservationLog> logs) {
        if (logs == null || logs.isEmpty()) return Collections.emptyMap();

        List<Long> ids = logs.stream().map(ObservationLog::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) return Collections.emptyMap();

        List<ObservationLogPostLink> links = linkMapper.selectList(new LambdaQueryWrapper<ObservationLogPostLink>()
            .eq(ObservationLogPostLink::getUserId, userId)
            .in(ObservationLogPostLink::getLogId, ids));

        Map<Long, Long> map = new HashMap<>();
        for (ObservationLogPostLink l : links) {
            map.put(l.getLogId(), l.getPostId());
        }
        return map;
    }

    private List<ObservationMediaVO> buildMediaVO(List<ObservationLogMedia> logMedia) {
        if (logMedia == null || logMedia.isEmpty()) return Collections.emptyList();

        List<ObservationMediaVO> out = new ArrayList<>();
        for (ObservationLogMedia lm : logMedia) {
            Media m = mediaBaseMapper.selectById(lm.getMediaId());
            if (m == null) continue;

            ObservationMediaVO vo = new ObservationMediaVO();
            vo.setId(m.getId());
            vo.setOriginUrl(m.getOriginUrl());
            vo.setThumbUrl(m.getThumbUrl());
            vo.setMediumUrl(m.getMediumUrl());
            vo.setMimeType(m.getMimeType());
            vo.setWidth(m.getWidth());
            vo.setHeight(m.getHeight());
            vo.setSizeBytes(m.getSizeBytes());
            vo.setSortOrder(lm.getSortOrder());
            out.add(vo);
        }
        return out;
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
