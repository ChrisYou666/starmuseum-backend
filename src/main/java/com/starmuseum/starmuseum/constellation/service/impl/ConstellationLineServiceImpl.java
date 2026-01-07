package com.starmuseum.starmuseum.constellation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.starmuseum.celestial.entity.CelestialBody;
import com.starmuseum.starmuseum.celestial.service.CelestialBodyService;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineCreateRequest;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineQueryRequest;
import com.starmuseum.starmuseum.constellation.vo.ConstellationLineSegment;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineUpdateRequest;
import com.starmuseum.starmuseum.constellation.entity.ConstellationLine;
import com.starmuseum.starmuseum.constellation.mapper.ConstellationLineMapper;
import com.starmuseum.starmuseum.constellation.service.ConstellationLineService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConstellationLineServiceImpl
        extends ServiceImpl<ConstellationLineMapper, ConstellationLine>
        implements ConstellationLineService {

    private final CelestialBodyService celestialBodyService;

    public ConstellationLineServiceImpl(CelestialBodyService celestialBodyService) {
        this.celestialBodyService = celestialBodyService;
    }

    @Override
    public Long createLine(ConstellationLineCreateRequest req) {
        validateBodyIds(req.getStartBodyId(), req.getEndBodyId());

        ConstellationLine line = new ConstellationLine();
        line.setConstellationCode(req.getConstellationCode());
        line.setConstellationName(req.getConstellationName());
        line.setStartBodyId(req.getStartBodyId());
        line.setEndBodyId(req.getEndBodyId());
        line.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        line.setRemark(req.getRemark());
        this.save(line);
        return line.getId();
    }

    @Override
    public boolean updateLine(ConstellationLineUpdateRequest req) {
        validateBodyIds(req.getStartBodyId(), req.getEndBodyId());

        ConstellationLine line = new ConstellationLine();
        line.setId(req.getId());
        line.setConstellationCode(req.getConstellationCode());
        line.setConstellationName(req.getConstellationName());
        line.setStartBodyId(req.getStartBodyId());
        line.setEndBodyId(req.getEndBodyId());
        line.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        line.setRemark(req.getRemark());
        return this.updateById(line);
    }

    @Override
    public IPage<ConstellationLine> page(ConstellationLineQueryRequest req) {
        int pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null ? 10 : req.getPageSize();

        LambdaQueryWrapper<ConstellationLine> qw = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(req.getConstellationCode())) {
            qw.eq(ConstellationLine::getConstellationCode, req.getConstellationCode().trim());
        }
        if (StringUtils.hasText(req.getConstellationName())) {
            qw.like(ConstellationLine::getConstellationName, req.getConstellationName().trim());
        }
        if (req.getStartBodyId() != null) {
            qw.eq(ConstellationLine::getStartBodyId, req.getStartBodyId());
        }
        if (req.getEndBodyId() != null) {
            qw.eq(ConstellationLine::getEndBodyId, req.getEndBodyId());
        }

        qw.orderByAsc(ConstellationLine::getConstellationCode)
                .orderByAsc(ConstellationLine::getSortOrder)
                .orderByAsc(ConstellationLine::getId);

        return this.page(new Page<>(pageNum, pageSize), qw);
    }

    @Override
    public List<ConstellationLineSegment> listSegments(String constellationCode) {
        LambdaQueryWrapper<ConstellationLine> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(constellationCode)) {
            qw.eq(ConstellationLine::getConstellationCode, constellationCode.trim());
        }
        qw.orderByAsc(ConstellationLine::getConstellationCode)
                .orderByAsc(ConstellationLine::getSortOrder)
                .orderByAsc(ConstellationLine::getId);

        List<ConstellationLine> lines = this.list(qw);

        return lines.stream().map(line -> {
            ConstellationLineSegment vo = new ConstellationLineSegment();
            vo.setId(line.getId());
            vo.setConstellationCode(line.getConstellationCode());
            vo.setConstellationName(line.getConstellationName());
            vo.setStartBodyId(line.getStartBodyId());
            vo.setEndBodyId(line.getEndBodyId());
            vo.setSortOrder(line.getSortOrder());
            return vo;
        }).collect(Collectors.toList());
    }

    private void validateBodyIds(Long startBodyId, Long endBodyId) {
        if (startBodyId == null || endBodyId == null) {
            throw new IllegalArgumentException("startBodyId/endBodyId 不能为空");
        }
        if (startBodyId.equals(endBodyId)) {
            throw new IllegalArgumentException("startBodyId 和 endBodyId 不能相同");
        }

        CelestialBody start = celestialBodyService.getById(startBodyId);
        if (start == null) {
            throw new IllegalArgumentException("startBodyId 对应的天体不存在: " + startBodyId);
        }
        CelestialBody end = celestialBodyService.getById(endBodyId);
        if (end == null) {
            throw new IllegalArgumentException("endBodyId 对应的天体不存在: " + endBodyId);
        }
    }
}