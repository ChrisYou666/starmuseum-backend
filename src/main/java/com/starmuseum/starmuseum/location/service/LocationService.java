package com.starmuseum.starmuseum.location.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starmuseum.starmuseum.location.dto.LocationCreateRequest;
import com.starmuseum.starmuseum.location.dto.LocationUpdateRequest;
import com.starmuseum.starmuseum.location.entity.Location;

public interface LocationService extends IService<Location> {

    Long create(LocationCreateRequest req);

    void update(Long id, LocationUpdateRequest req);

    void delete(Long id);

    Location detail(Long id);

    IPage<Location> page(long pageNum, long pageSize, String keyword);
}