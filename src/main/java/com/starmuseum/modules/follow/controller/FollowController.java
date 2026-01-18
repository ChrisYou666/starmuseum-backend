package com.starmuseum.modules.follow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.follow.service.UserFollowService;
import com.starmuseum.modules.follow.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

    private final UserFollowService userFollowService;

    @PostMapping("/{userId}")
    public Result<Void> follow(@PathVariable Long userId) {
        Long uid = CurrentUser.requireUserId();
        userFollowService.follow(uid, userId);
        return Result.ok();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId) {
        Long uid = CurrentUser.requireUserId();
        userFollowService.unfollow(uid, userId);
        return Result.ok();
    }

    @GetMapping("/is-following/{userId}")
    public Result<Boolean> isFollowing(@PathVariable Long userId) {
        Long uid = CurrentUser.getUserIdOrNull();
        return Result.ok(userFollowService.isFollowing(uid, userId));
    }

    @GetMapping("/my/following")
    public Result<IPage<FollowUserVO>> myFollowing(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(userFollowService.myFollowingPage(uid, page, size));
    }

    @GetMapping("/my/followers")
    public Result<IPage<FollowUserVO>> myFollowers(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(userFollowService.myFollowersPage(uid, page, size));
    }
}
