package com.starmuseum.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子主表
 *
 * 表：post
 */
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 正文
     */
    private String content;

    /**
     * 可见性：PUBLIC / PRIVATE / FOLLOWERS(预留)
     */
    private String visibility;

    private Integer likeCount;
    private Integer commentCount;

    /**
     * 软删字段
     */
    private LocalDateTime deletedAt;
    private Long deletedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
