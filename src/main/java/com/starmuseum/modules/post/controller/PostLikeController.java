package com.starmuseum.modules.post.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.post.service.PostLikeService;
import com.starmuseum.modules.post.vo.PostLikeResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/post")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    /**
     * 点赞
     * POST /api/post/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public Result<PostLikeResponse> like(@PathVariable Long postId) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(postLikeService.like(postId, uid));
    }

    /**
     * 取消点赞
     * DELETE /api/post/{postId}/like
     */
    @DeleteMapping("/{postId}/like")
    public Result<PostLikeResponse> unlike(@PathVariable Long postId) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(postLikeService.unlike(postId, uid));
    }
}
