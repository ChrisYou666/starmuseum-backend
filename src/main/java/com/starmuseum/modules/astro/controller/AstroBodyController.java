package com.starmuseum.modules.astro.controller;

import com.starmuseum.modules.astro.service.CatalogVersionService;
import com.starmuseum.modules.astro.service.SkyService;
import com.starmuseum.modules.astro.vo.BodyDetailVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/astro/body")
@Validated
public class AstroBodyController {

    private final SkyService skyService;
    private final CatalogVersionService catalogVersionService;

    public AstroBodyController(SkyService skyService,
                               CatalogVersionService catalogVersionService) {
        this.skyService = skyService;
        this.catalogVersionService = catalogVersionService;
    }

    /**
     * 示例：
     * GET /api/astro/body/1?time=2026-01-10T12:00:00Z&lat=31.2304&lon=121.4737
     */
    @GetMapping("/{id}")
    public BodyDetailVO detail(@PathVariable("id") long id,
                               @RequestParam("time") @NotBlank String time,
                               @RequestParam("lat") @Min(-90) @Max(90) double lat,
                               @RequestParam("lon") @Min(-180) @Max(180) double lon) {

        Instant timeUtc = Instant.parse(time);
        String catalogVersionCode = catalogVersionService.getActiveCatalogVersionCode();

        BodyDetailVO vo = skyService.getBodyDetail(catalogVersionCode, id, timeUtc, lat, lon);
        if (vo == null) {
            // 先简单返回 null（后面我们再统一错误码/Result）
            return null;
        }
        return vo;
    }
}
