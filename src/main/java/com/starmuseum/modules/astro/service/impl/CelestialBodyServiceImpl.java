package com.starmuseum.modules.astro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.mapper.CelestialBodyMapper;
import com.starmuseum.modules.astro.service.CelestialBodyService;
import com.starmuseum.modules.astro.vo.BodySearchItemVO;
import com.starmuseum.modules.astro.vo.CelestialBodyDetailResponse;
import com.starmuseum.modules.astro.vo.CelestialBodySkyItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CelestialBodyServiceImpl implements CelestialBodyService {

    private final CelestialBodyMapper celestialBodyMapper;

    @Override
    public List<CelestialBodySkyItem> listSkyBodies(String catalogVersionCode, String bodyType, int limit) {
        LambdaQueryWrapper<CelestialBody> w = new LambdaQueryWrapper<>();
        w.eq(CelestialBody::getCatalogVersionCode, catalogVersionCode);
        w.eq(CelestialBody::getBodyType, bodyType);
        w.orderByAsc(CelestialBody::getMag);
        w.last("limit " + limit);

        List<CelestialBody> list = celestialBodyMapper.selectList(w);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream().map(b -> {
            CelestialBodySkyItem item = new CelestialBodySkyItem();
            item.setId(b.getId());
            item.setCatalogCode(b.getCatalogCode());
            item.setBodyType(b.getBodyType());
            item.setName(b.getName());
            item.setNameZh(b.getNameZh());
            item.setNameEn(b.getNameEn());
            item.setNameId(b.getNameId());
            item.setRaDeg(b.getRaDeg());
            item.setDecDeg(b.getDecDeg());
            item.setMag(b.getMag());
            item.setConstellation(b.getConstellation());
            // 注意：CelestialBodySkyItem 里要有这两个字段才可以 set（你现在是“原本类”就不要 set）
            // item.setSpectralType(b.getSpectralType());
            // item.setWikiUrl(b.getWikiUrl());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CelestialBody> listBrightStars(String catalogVersionCode, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));

        LambdaQueryWrapper<CelestialBody> w = new LambdaQueryWrapper<>();
        w.eq(CelestialBody::getCatalogVersionCode, catalogVersionCode);
        w.eq(CelestialBody::getBodyType, "STAR");
        w.isNotNull(CelestialBody::getRaDeg);
        w.isNotNull(CelestialBody::getDecDeg);
        w.orderByAsc(CelestialBody::getMag);
        w.last("limit " + safeLimit);

        return celestialBodyMapper.selectList(w);
    }


    @Override
    public CelestialBody getById(Long id) {
        return celestialBodyMapper.selectById(id);
    }

    @Override
    public CelestialBodyDetailResponse getDetail(Long id, String time, double lat, double lon) {
        // 你项目里原本应该已经有详情计算逻辑，这里不要动你原实现。
        // 你把你当前 getDetail 的实现粘过来，我给你合并到这个最终版本里。
        throw new UnsupportedOperationException("请把你当前 getDetail 实现粘贴出来，我帮你合并成最终版");
    }

    @Override
    public List<BodySearchItemVO> search(String catalogVersionCode, String q, Integer limit, Long offset) {
        if (!StringUtils.hasText(catalogVersionCode)) {
            return Collections.emptyList();
        }
        if (!StringUtils.hasText(q)) {
            return Collections.emptyList();
        }

        int l = (limit == null) ? 20 : limit;
        if (l < 1) l = 1;
        if (l > 100) l = 100;

        // Controller 校验 offset 最小 1；这里转换为 SQL OFFSET（从0开始）
        long off = 0;
        if (offset != null) {
            off = Math.max(0, offset - 1);
        }

        return celestialBodyMapper.search(catalogVersionCode, q.trim(), l, off);
    }

}
