package com.starmuseum.starmuseum.constellation.controller;

import com.starmuseum.starmuseum.common.Result;
import com.starmuseum.starmuseum.constellation.dto.ConstellationOptionResponse;
import com.starmuseum.starmuseum.constellation.service.ConstellationStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 星座统计/选项类接口
 */
@RestController
public class ConstellationStatsController {

    private final ConstellationStatsService constellationStatsService;

    public ConstellationStatsController(ConstellationStatsService constellationStatsService) {
        this.constellationStatsService = constellationStatsService;
    }

    /**
     * 星座下拉选项：去重 + 返回线段数量
     * GET /api/constellation-lines/options
     */
    @GetMapping("/api/constellation-lines/options")
    public Result<List<ConstellationOptionResponse>> options() {
        return Result.success(constellationStatsService.listOptions());
    }
}
