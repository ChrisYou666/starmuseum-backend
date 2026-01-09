package com.starmuseum.modules.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.post.dto.PostCommentCreateRequest;
import com.starmuseum.modules.post.vo.PostCommentResponse;

import java.util.List;

public interface PostCommentService {

    PostCommentResponse create(Long postId, Long userId, PostCommentCreateRequest req);

    IPage<PostCommentResponse> page(Long postId, int page, int size);

    void deleteMy(Long commentId, Long userId);

    long countByPostId(Long postId);

    /**
     * 取最新 N 条评论（用于 Post 详情页展示）
     */
    List<PostCommentResponse> latest(Long postId, int limit);
}
