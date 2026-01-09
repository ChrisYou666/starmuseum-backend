package com.starmuseum.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("post_comment")
@Data
public class PostComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long userId;

    private String content;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
