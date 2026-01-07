package com.starmuseum.starmuseum.celestial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.starmuseum.celestial.dto.CelestialBodyQueryRequest;
import com.starmuseum.starmuseum.celestial.entity.CelestialBody;
import com.starmuseum.starmuseum.celestial.enums.CelestialType;
import com.starmuseum.starmuseum.celestial.mapper.CelestialBodyMapper;
import com.starmuseum.starmuseum.celestial.service.CelestialBodyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CelestialBodyServiceImpl extends ServiceImpl<CelestialBodyMapper, CelestialBody>
        implements CelestialBodyService {

    @Override
    public IPage<CelestialBody> pageQuery(Integer pageNum, Integer pageSize, CelestialBodyQueryRequest query) {
        Page<CelestialBody> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<CelestialBody> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                String kw = query.getKeyword().trim();
                wrapper.and(w -> w.like(CelestialBody::getName, kw)
                        .or().like(CelestialBody::getAlias, kw)
                        .or().like(CelestialBody::getConstellation, kw)
                        .or().like(CelestialBody::getSpectralType, kw)
                        .or().like(CelestialBody::getDescription, kw));
            }

            CelestialType type = query.getType();
            if (type != null) {
                wrapper.eq(CelestialBody::getType, type);
            }

            if (StringUtils.hasText(query.getConstellation())) {
                wrapper.eq(CelestialBody::getConstellation, query.getConstellation().trim());
            }

            if (query.getMinMagnitude() != null) {
                wrapper.ge(CelestialBody::getMagnitude, query.getMinMagnitude());
            }
            if (query.getMaxMagnitude() != null) {
                wrapper.le(CelestialBody::getMagnitude, query.getMaxMagnitude());
            }

            String orderBy = query.getOrderBy();
            if (!StringUtils.hasText(orderBy) || "created_at".equalsIgnoreCase(orderBy)) {
                wrapper.orderByDesc(CelestialBody::getCreatedAt);
            } else if ("magnitude".equalsIgnoreCase(orderBy)) {
                // 越小越亮，默认按“更亮优先”
                wrapper.orderByAsc(CelestialBody::getMagnitude);
            } else if ("distance_ly".equalsIgnoreCase(orderBy) || "distanceLy".equalsIgnoreCase(orderBy)) {
                wrapper.orderByAsc(CelestialBody::getDistanceLy);
            } else {
                wrapper.orderByDesc(CelestialBody::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(CelestialBody::getCreatedAt);
        }

        return this.page(page, wrapper);
    }
}