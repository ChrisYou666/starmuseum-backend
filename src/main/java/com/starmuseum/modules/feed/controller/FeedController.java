package com.starmuseum.modules.feed.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.feed.enums.FeedMode;
import com.starmuseum.modules.feed.service.FeedService;
import com.starmuseum.modules.post.vo.PostDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    /**
     * 推荐 Feed
     * - mode=FOLLOW：只看关注（需要登录）
     * - mode=HOT：热门（允许匿名）
     * - mode=MIX：首屏关注优先，不足热门补（需要登录；page>1 简化为 HOT）
     */
    @GetMapping("/recommend")
    public Result<IPage<PostDetailResponse>> recommend(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(defaultValue = "MIX") String mode) {

        FeedMode m;
        try {
            m = FeedMode.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            m = FeedMode.MIX;
        }

        Long uid = CurrentUser.getUserIdOrNull();
        return Result.ok(feedService.recommend(page, size, m, uid));
    }
}
