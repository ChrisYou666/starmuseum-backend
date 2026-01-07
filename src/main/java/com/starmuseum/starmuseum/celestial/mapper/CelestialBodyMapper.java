package com.starmuseum.starmuseum.celestial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starmuseum.starmuseum.celestial.entity.CelestialBody;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CelestialBodyMapper extends BaseMapper<CelestialBody> {
}