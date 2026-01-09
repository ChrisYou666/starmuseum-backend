package com.starmuseum.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子-图片关系表
 *
 * 表：post_media
 */
@Data
@TableName("post_media")
public class PostMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long mediaId;

    /**
     * 排序号：1..N
     */
    private Integer sortNo;

    private LocalDateTime createdAt;
}
