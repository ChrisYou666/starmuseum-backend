package com.starmuseum.starmuseum.celestial.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starmuseum.starmuseum.celestial.dto.CelestialBodyQueryRequest;
import com.starmuseum.starmuseum.celestial.entity.CelestialBody;

public interface CelestialBodyService extends IService<CelestialBody> {

    /**
     * 分页查询（支持关键字、类型、星座、视星等区间过滤）。
     */
    IPage<CelestialBody> pageQuery(Integer pageNum, Integer pageSize, CelestialBodyQueryRequest query);
}