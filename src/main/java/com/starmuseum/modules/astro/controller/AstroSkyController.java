package com.starmuseum.modules.astro.controller;

import com.starmuseum.modules.astro.dto.SkySummaryRequest;
import com.starmuseum.modules.astro.service.CatalogVersionService;
import com.starmuseum.modules.astro.service.SkyService;
import com.starmuseum.modules.astro.vo.StarPositionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/astro/sky")
public class AstroSkyController {

    private final SkyService skyService;
    private final CatalogVersionService catalogVersionService;

    public AstroSkyController(SkyService skyService,
                              CatalogVersionService catalogVersionService) {
        this.skyService = skyService;
        this.catalogVersionService = catalogVersionService;
    }

    @GetMapping("/summary")
    public com.starmuseum.modules.astro.vo.SkySummaryResponse summary(@Valid SkySummaryRequest req) {

        int limit = (req.getLimit() == null) ? 50 : req.getLimit();
        boolean visibleOnly = Boolean.TRUE.equals(req.getVisibleOnly());

        String sort = req.getSort();
        if (sort == null || sort.isBlank()) {
            sort = "mag";
        }

        String catalogVersionCode = catalogVersionService.getActiveCatalogVersionCode();

        Instant timeUtc = Instant.parse(req.getTime());

        List<StarPositionVO> list = skyService.listBrightStarPositions(
            catalogVersionCode,
            timeUtc,
            req.getLat(),
            req.getLon(),
            limit
        );

        // ✅ 关键：无论是否过滤，先转成可变 List，避免后面 sort 报错
        list = new ArrayList<>(list);

        // 过滤：只返回可见天体（altitude > 0）
        if (visibleOnly) {
            List<StarPositionVO> filtered = new ArrayList<>();
            for (StarPositionVO s : list) {
                if (Boolean.TRUE.equals(s.getVisible())) {
                    filtered.add(s);
                }
            }
            list = filtered;
        }

        // 排序：alt（高度从高到低） / 默认 mag（亮到暗，数值越小越亮）
        if ("alt".equalsIgnoreCase(sort)) {
            list.sort((a, b) -> {
                double aAlt = (a.getAltitudeDeg() == null) ? -999d : a.getAltitudeDeg();
                double bAlt = (b.getAltitudeDeg() == null) ? -999d : b.getAltitudeDeg();
                return Double.compare(bAlt, aAlt); // 降序
            });
        } else {
            list.sort((a, b) -> {
                double aMag = (a.getMag() == null) ? 999d : a.getMag();
                double bMag = (b.getMag() == null) ? 999d : b.getMag();
                return Double.compare(aMag, bMag); // 升序
            });
        }

        com.starmuseum.modules.astro.vo.SkySummaryResponse resp = new com.starmuseum.modules.astro.vo.SkySummaryResponse();

        com.starmuseum.modules.astro.vo.SkySummaryResponse.Meta meta =
            new com.starmuseum.modules.astro.vo.SkySummaryResponse.Meta();
        meta.setCatalogVersionCode(catalogVersionCode);
        meta.setTime(req.getTime());
        meta.setLat(req.getLat());
        meta.setLon(req.getLon());
        meta.setRequestedLimit(limit);
        meta.setVisibleOnly(visibleOnly);
        meta.setSort(sort);
        meta.setTotal(list.size());

        resp.setMeta(meta);
        resp.setItems(list);

        return resp;
    }
}
