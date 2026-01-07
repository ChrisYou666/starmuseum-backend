package com.starmuseum.starmuseum.constellation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineCreateRequest;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineQueryRequest;
import com.starmuseum.starmuseum.constellation.vo.ConstellationLineSegment;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineUpdateRequest;
import com.starmuseum.starmuseum.constellation.entity.ConstellationLine;

import java.util.List;

public interface ConstellationLineService extends IService<ConstellationLine> {

    Long createLine(ConstellationLineCreateRequest req);

    boolean updateLine(ConstellationLineUpdateRequest req);

    IPage<ConstellationLine> page(ConstellationLineQueryRequest req);

    /**
     * SkyView 用：返回线段（startBodyId/endBodyId）
     * @param constellationCode 可空；为空返回全部
     */
    List<ConstellationLineSegment> listSegments(String constellationCode);
}