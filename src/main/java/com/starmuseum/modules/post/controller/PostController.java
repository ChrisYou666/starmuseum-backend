package com.starmuseum.modules.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.post.dto.PostCreateRequest;
import com.starmuseum.modules.post.service.PostService;
import com.starmuseum.modules.post.vo.PostDetailResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 创建帖子（携带 mediaIds）
     */
    @PostMapping
    public Result<PostDetailResponse> create(@RequestBody @Valid PostCreateRequest req) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(postService.create(req, uid));
    }

    /**
     * 帖子详情（返回图片列表）
     */
    @GetMapping("/{id}")
    public Result<PostDetailResponse> detail(@PathVariable Long id) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(postService.detail(id, uid));
    }

    /**
     * 我的帖子分页
     */
    @GetMapping("/my/page")
    public Result<IPage<PostDetailResponse>> myPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(postService.myPage(page, size, uid));
    }

    /**
     * 删除帖子（软删）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long uid = CurrentUser.requireUserId();
        postService.deleteMy(id, uid);
        return Result.ok(null);
    }

    // 公共信息流
    @GetMapping("/page")
    public Result<IPage<PostDetailResponse>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return Result.ok(postService.publicPage(page, size));
    }

    // 他人主页
    @GetMapping("/user/{userId}/page")
    public Result<IPage<PostDetailResponse>> userPage(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return Result.ok(postService.userPage(page, size, userId));
    }

}
