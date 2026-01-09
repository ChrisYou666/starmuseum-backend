package com.starmuseum.modules.post.service;

import com.starmuseum.modules.post.vo.PostLikeResponse;

public interface PostLikeService {

    /**
     * 点赞
     */
    PostLikeResponse like(Long postId, Long userId);

    /**
     * 取消点赞
     */
    PostLikeResponse unlike(Long postId, Long userId);

    /**
     * 是否已点赞
     */
    boolean isLiked(Long postId, Long userId);
}
