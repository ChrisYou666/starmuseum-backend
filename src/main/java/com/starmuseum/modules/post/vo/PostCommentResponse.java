package com.starmuseum.modules.post.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论返回
 */
@Data
public class PostCommentResponse {

    private Long id;
    private Long postId;

    private Long userId;
    private String nickname;
    private String avatarUrl;

    private String content;
    private LocalDateTime createdAt;
}
