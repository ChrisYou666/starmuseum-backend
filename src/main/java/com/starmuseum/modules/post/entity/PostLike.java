package com.starmuseum.modules.post.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * post_like 表：帖子点赞
 * - 一个用户对一个帖子最多点赞一次（依赖 uk_post_user 唯一索引）
 */
@TableName("post_like")
@Data
public class PostLike {

    @TableId
    private Long id;

    private Long postId;

    private Long userId;

    private LocalDateTime createdAt;
}
