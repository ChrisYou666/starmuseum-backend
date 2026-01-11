package com.starmuseum.modules.astro.service;

import com.starmuseum.modules.astro.vo.BodyDetailVO;
import com.starmuseum.modules.astro.vo.StarPositionVO;

import java.time.Instant;
import java.util.List;

public interface SkyService {

    /**
     * 亮星位置列表（用于 /api/astro/sky/summary）
     */
    List<StarPositionVO> listBrightStarPositions(String catalogVersionCode,
                                                 Instant timeUtc,
                                                 double latDeg,
                                                 double lonDeg,
                                                 int limit);

    /**
     * 天体详情（用于 /api/astro/body/{id}）
     */
    BodyDetailVO getBodyDetail(String catalogVersionCode,
                               long id,
                               Instant timeUtc,
                               double latDeg,
                               double lonDeg);
}
