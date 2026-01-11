package com.starmuseum.modules.astro.service;

import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.vo.BodySearchItemVO;
import com.starmuseum.modules.astro.vo.CelestialBodyDetailResponse;
import com.starmuseum.modules.astro.vo.CelestialBodySkyItem;

import java.util.List;

public interface CelestialBodyService {

    List<CelestialBodySkyItem> listSkyBodies(String catalogVersionCode, String bodyType, int limit);

    List<CelestialBody> listBrightStars(String catalogVersionCode, int limit);

    CelestialBody getById(Long id);

    CelestialBodyDetailResponse getDetail(Long id, String time, double lat, double lon);

    /**
     * 天体搜索（阶段2：MVP）
     * 说明：
     * - 由 Controller 传入 active catalogVersionCode
     * - q 支持：catalog_code / name / name_zh/en/id / alias_name
     * - limit: 1~100
     * - offset: Controller 校验最小1，这里按“从1开始的偏移”转成 SQL OFFSET（从0开始）
     */
    List<BodySearchItemVO> search(String catalogVersionCode, String q, Integer limit, Long offset);

}
