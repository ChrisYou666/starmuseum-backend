package com.starmuseum.starmuseum.location.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.starmuseum.common.exception.BusinessException;
import com.starmuseum.starmuseum.location.dto.LocationCreateRequest;
import com.starmuseum.starmuseum.location.dto.LocationUpdateRequest;
import com.starmuseum.starmuseum.location.entity.Location;
import com.starmuseum.starmuseum.location.mapper.LocationMapper;
import com.starmuseum.starmuseum.location.service.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {

    @Override
    public Long create(LocationCreateRequest req) {
        // 唯一性校验（name）
        boolean exists = this.lambdaQuery()
                .eq(Location::getName, req.getName())
                .exists();
        if (exists) {
            throw BusinessException.badRequest("地点名称已存在：" + req.getName());
        }

        Location entity = new Location();
        entity.setName(req.getName());
        entity.setCountry(req.getCountry());
        entity.setProvince(req.getProvince());
        entity.setCity(req.getCity());
        entity.setLatitude(req.getLatitude());
        entity.setLongitude(req.getLongitude());
        entity.setTimezone(req.getTimezone());
        entity.setAltitudeM(req.getAltitudeM());
        entity.setRemark(req.getRemark());

        this.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, LocationUpdateRequest req) {
        Location db = this.getById(id);
        if (db == null) {
            throw BusinessException.notFound("地点不存在，id=" + id);
        }

        // name 唯一性校验（排除自身）
        boolean exists = this.lambdaQuery()
                .eq(Location::getName, req.getName())
                .ne(Location::getId, id)
                .exists();
        if (exists) {
            throw BusinessException.badRequest("地点名称已存在：" + req.getName());
        }

        db.setName(req.getName());
        db.setCountry(req.getCountry());
        db.setProvince(req.getProvince());
        db.setCity(req.getCity());
        db.setLatitude(req.getLatitude());
        db.setLongitude(req.getLongitude());
        db.setTimezone(req.getTimezone());
        db.setAltitudeM(req.getAltitudeM());
        db.setRemark(req.getRemark());

        this.updateById(db);
    }

    @Override
    public void delete(Long id) {
        boolean ok = this.removeById(id);
        if (!ok) {
            throw BusinessException.notFound("地点不存在或已删除，id=" + id);
        }
    }

    @Override
    public Location detail(Long id) {
        Location db = this.getById(id);
        if (db == null) {
            throw BusinessException.notFound("地点不存在，id=" + id);
        }
        return db;
    }

    @Override
    public IPage<Location> page(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<Location> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Location::getName, keyword)
                    .or().like(Location::getCity, keyword)
                    .or().like(Location::getCountry, keyword));
        }
        qw.orderByDesc(Location::getUpdatedAt);

        return this.page(new Page<>(pageNum, pageSize), qw);
    }
}