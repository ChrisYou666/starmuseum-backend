package com.starmuseum.starmuseum.location.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starmuseum.starmuseum.location.entity.Location;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocationMapper extends BaseMapper<Location> {
}