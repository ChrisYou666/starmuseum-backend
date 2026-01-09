package com.starmuseum.modules.post.vo;

import lombok.Data;

/**
 * 点赞/取消点赞 返回
 */
@Data
public class PostLikeResponse {

    private Long postId;

    /**
     * true=已点赞，false=已取消
     */
    private Boolean liked;

    /**
     * 最新点赞数（来自 post.like_count）
     */
    private Integer likeCount;
}
