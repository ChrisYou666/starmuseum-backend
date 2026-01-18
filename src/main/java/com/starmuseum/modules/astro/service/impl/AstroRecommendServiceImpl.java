package com.starmuseum.modules.astro.service.impl;

import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.mapper.CelestialBodyMapper;
import com.starmuseum.modules.astro.service.AstroRecommendService;
import com.starmuseum.modules.astro.service.CatalogVersionService;
import com.starmuseum.modules.astro.util.AstronomyCalculator;
import com.starmuseum.modules.astro.dto.AstroRecommendRequest;
import com.starmuseum.modules.astro.vo.AstroRecommendItemVO;
import com.starmuseum.modules.observation.mapper.ObservationTargetStatsMapper;
import com.starmuseum.modules.observation.vo.TargetCountRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AstroRecommendServiceImpl implements AstroRecommendService {

    private final CatalogVersionService catalogVersionService;
    private final CelestialBodyMapper celestialBodyMapper;
    private final ObservationTargetStatsMapper observationTargetStatsMapper;

    @Override
    public List<AstroRecommendItemVO> recommend(AstroRecommendRequest req, Long currentUserIdOrNull) {
        if (req == null) throw new BizException(400, "请求不能为空");
        if (req.getLat() == null || req.getLon() == null) throw new BizException(400, "lat/lon 不能为空");
        if (req.getLat() < -90 || req.getLat() > 90) throw new BizException(400, "lat 超出范围[-90,90]");
        if (req.getLon() < -180 || req.getLon() > 180) throw new BizException(400, "lon 超出范围[-180,180]");

        int top = (req.getTop() == null) ? 20 : Math.max(1, Math.min(req.getTop(), 50));
        boolean includeNotVisible = Boolean.TRUE.equals(req.getIncludeNotVisible());

        Instant timeUtc;
        try {
            timeUtc = Instant.parse(req.getTime());
        } catch (DateTimeParseException e) {
            // 兼容带 offset 的格式
            try {
                timeUtc = OffsetDateTime.parse(req.getTime()).toInstant();
            } catch (Exception ex) {
                throw new BizException(400, "time 格式不正确，需 ISO-8601 且带时区，例如：2026-01-10T12:00:00Z");
            }
        }

        String activeCatalog = catalogVersionService.getActiveCatalogVersionCode();
        if (activeCatalog == null || activeCatalog.isBlank()) {
            throw new BizException(409, "当前没有 ACTIVE catalog 版本");
        }

        // 统计窗口（可调）：个人偏好 180 天，社区热门 30 天
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime userFrom = now.minusDays(180);
        LocalDateTime userTo = now.plusSeconds(1);

        LocalDateTime communityFrom = now.minusDays(30);
        LocalDateTime communityTo = now.plusSeconds(1);

        List<TargetCountRow> personal = (currentUserIdOrNull == null)
            ? Collections.emptyList()
            : observationTargetStatsMapper.selectUserTopTargets(currentUserIdOrNull, userFrom, userTo, 80);

        List<TargetCountRow> community = observationTargetStatsMapper.selectCommunityTopTargets(communityFrom, communityTo, 200);

        Map<Long, Long> personalCnt = personal.stream()
            .filter(r -> r.getTargetId() != null)
            .collect(Collectors.toMap(TargetCountRow::getTargetId, r -> nvl(r.getCnt()), (a, b) -> a));

        Map<Long, Long> communityCnt = community.stream()
            .filter(r -> r.getTargetId() != null)
            .collect(Collectors.toMap(TargetCountRow::getTargetId, r -> nvl(r.getCnt()), (a, b) -> a));

        // 候选集：个人优先 + 社区补充
        LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
        personal.forEach(r -> candidateIds.add(r.getTargetId()));
        community.forEach(r -> candidateIds.add(r.getTargetId()));

        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查天体信息
        List<CelestialBody> bodies = celestialBodyMapper.selectBatchIds(new ArrayList<>(candidateIds));
        if (bodies == null || bodies.isEmpty()) return Collections.emptyList();

        double lat = req.getLat();
        double lon = req.getLon();

        List<AstroRecommendItemVO> items = new ArrayList<>(bodies.size());
        for (CelestialBody b : bodies) {
            if (b == null) continue;
            if (b.getCatalogVersionCode() != null && !activeCatalog.equals(b.getCatalogVersionCode())) {
                continue; // 只推荐当前激活 catalog 的数据
            }
            if (b.getRaDeg() == null || b.getDecDeg() == null) continue;

            AstronomyCalculator.AltAz altAz = AstronomyCalculator.equatorialToHorizontal(
                timeUtc, lat, lon, b.getRaDeg(), b.getDecDeg()
            );

            if (!includeNotVisible && !altAz.visible()) {
                continue;
            }

            long pCnt = personalCnt.getOrDefault(b.getId(), 0L);
            long cCnt = communityCnt.getOrDefault(b.getId(), 0L);

            double typeWeight = typeWeight(b.getBodyType());
            double visibleBonus = altAz.visible() ? 10.0 : 0.0;
            double magPenalty = (b.getMag() == null) ? 0.0 : Math.max(0.0, b.getMag()) * 0.3;

            // 综合分：个人偏好更高权重，其次社区热度，再加可见奖励，减去视星等惩罚
            double score = (pCnt * 3.0 + cCnt * 1.0) * typeWeight + visibleBonus - magPenalty;

            AstroRecommendItemVO vo = new AstroRecommendItemVO();
            vo.setId(b.getId());
            vo.setCatalogCode(b.getCatalogCode());
            vo.setBodyType(b.getBodyType());

            vo.setName(b.getName());
            vo.setNameZh(b.getNameZh());
            vo.setNameEn(b.getNameEn());
            vo.setNameId(b.getNameId());

            vo.setMag(b.getMag());
            vo.setConstellation(b.getConstellation());

            vo.setAltitudeDeg(altAz.altitudeDeg());
            vo.setAzimuthDeg(altAz.azimuthDeg());
            vo.setVisible(altAz.visible());

            vo.setScore(score);
            vo.setReason(pCnt > 0 && cCnt > 0 ? "MIX" : (pCnt > 0 ? "PERSONAL" : "COMMUNITY"));

            items.add(vo);
        }

        items.sort((a, b) -> Double.compare(nvl(b.getScore()), nvl(a.getScore())));
        if (items.size() > top) {
            return items.subList(0, top);
        }
        return items;
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private double nvl(Double v) {
        return v == null ? 0.0 : v;
    }

    private double typeWeight(String bodyType) {
        if (bodyType == null) return 1.0;
        String t = bodyType.trim().toUpperCase();
        // MVP：DSO/PLANET 权重略高，符合“偏好叠加”
        if (t.equals("DSO")) return 2.0;
        if (t.equals("PLANET")) return 1.5;
        return 1.0;
    }
}
