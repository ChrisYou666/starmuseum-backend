package com.starmuseum.modules.stats.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.stats.dto.StatsRangeRequest;
import com.starmuseum.modules.stats.service.StatsService;
import com.starmuseum.modules.stats.vo.MyStatsResponse;
import com.starmuseum.modules.stats.vo.TargetHotItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 阶段5C：统计（聚合查询版，不建汇总表）
 * - 我的统计：本月观测次数/成功率/评分均值/方式分布/常观测目标TopN
 * - 社区热门：热门目标TopN
 *
 * 所有接口需要登录（沿用全局 SecurityConfig）
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 我的统计（按时间范围）
     * 建议前端默认传本月起止，也可用 /me/month 自动生成本月范围
     */
    @PostMapping("/me/range")
    public Result<MyStatsResponse> myRange(@Valid @RequestBody StatsRangeRequest req) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(statsService.myStats(uid, req));
    }

    /**
     * 我的统计（本月）
     * month 格式：YYYY-MM（可不传，不传则取当前月）
     */
    @GetMapping("/me/month")
    public Result<MyStatsResponse> myMonth(@RequestParam(value = "month", required = false) String month,
                                           @RequestParam(value = "top", required = false, defaultValue = "10") int top) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(statsService.myMonth(uid, month, top));
    }

    /**
     * 社区热门目标（按时间范围）
     * from/to 建议 ISO8601：2026-01-01T00:00:00
     */
    @PostMapping("/hot/targets")
    public Result<List<TargetHotItemVO>> hotTargets(@Valid @RequestBody StatsRangeRequest req,
                                                    @RequestParam(value = "top", required = false, defaultValue = "10") int top) {
        return Result.ok(statsService.hotTargets(req, top));
    }
}
