package com.starmuseum.modules.astro.controller;

import com.starmuseum.modules.astro.service.CatalogVersionService;
import com.starmuseum.modules.astro.service.CelestialBodyService;
import com.starmuseum.modules.astro.vo.BodySearchItemVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/astro")
public class AstroSearchController {

    private final CelestialBodyService celestialBodyService;
    private final CatalogVersionService catalogVersionService;

    public AstroSearchController(CelestialBodyService celestialBodyService,
                                 CatalogVersionService catalogVersionService) {
        this.celestialBodyService = celestialBodyService;
        this.catalogVersionService = catalogVersionService;
    }

    /**
     * 天体搜索（阶段2：MVP）
     * - 默认仅搜索当前 ACTIVE 的 catalog_version_code
     */
    @GetMapping("/search")
    public List<BodySearchItemVO> search(
        @RequestParam @NotBlank(message = "q 不能为空") String q,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "limit 最小为1") @Max(value = 100, message = "limit 最大为100") Integer limit,
        @RequestParam(required = false) @Min(value = 1, message = "offset 最小为1") Long offset
    ) {
        String activeCode = catalogVersionService.getActiveCatalogVersionCode();
        return celestialBodyService.search(activeCode, q, limit, offset);
    }
}
