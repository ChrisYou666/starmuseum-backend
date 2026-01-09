package com.starmuseum.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论请求
 */
@Data
public class PostCommentCreateRequest {

    @NotBlank
    @Size(max = 1000)
    private String content;
}
