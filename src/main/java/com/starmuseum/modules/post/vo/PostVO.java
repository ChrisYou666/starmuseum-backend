package com.starmuseum.modules.post.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String content;
    private String visibility;

    private Integer likeCount;
    private Integer commentCount;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PostMediaVO> medias;
}
