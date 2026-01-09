package com.starmuseum.modules.post.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.post.dto.PostCommentCreateRequest;
import com.starmuseum.modules.post.service.PostCommentService;
import com.starmuseum.modules.post.vo.PostCommentResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/post")
public class PostCommentController {

    private final PostCommentService commentService;

    public PostCommentController(PostCommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 创建评论
     * POST /api/post/{postId}/comment
     */
    @PostMapping("/{postId}/comment")
    public Result<PostCommentResponse> create(
        @PathVariable Long postId,
        @RequestBody @Valid PostCommentCreateRequest req
    ) {
        Long uid = CurrentUser.requireUserId();
        return Result.ok(commentService.create(postId, uid, req));
    }

    /**
     * 评论分页
     * GET /api/post/{postId}/comment/page?page=1&size=10
     */
    @GetMapping("/{postId}/comment/page")
    public Result<IPage<PostCommentResponse>> page(
        @PathVariable Long postId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        CurrentUser.requireUserId(); // 需要登录（你系统默认这样控制）
        return Result.ok(commentService.page(postId, page, size));
    }

    /**
     * 删除自己的评论（软删）
     * DELETE /api/post/comment/{commentId}
     */
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId) {
        Long uid = CurrentUser.requireUserId();
        commentService.deleteMy(commentId, uid);
        return Result.ok(null);
    }
}
