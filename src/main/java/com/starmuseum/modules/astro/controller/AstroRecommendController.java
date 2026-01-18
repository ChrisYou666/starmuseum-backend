package com.starmuseum.modules.astro.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.astro.dto.AstroRecommendRequest;
import com.starmuseum.modules.astro.service.AstroRecommendService;
import com.starmuseum.modules.astro.vo.AstroRecommendItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/astro")
public class AstroRecommendController {

    private final AstroRecommendService astroRecommendService;

    /**
     * 星空推荐（因地因时 + 个人偏好）
     * - 未登录也可用（仅社区热门）
     * - 登录后会叠加个人偏好（observation_log_target）
     */
    @PostMapping("/recommend")
    public Result<List<AstroRecommendItemVO>> recommend(@Valid @RequestBody AstroRecommendRequest req) {
        Long uid = CurrentUser.getUserIdOrNull();
        return Result.ok(astroRecommendService.recommend(req, uid));
    }
}
