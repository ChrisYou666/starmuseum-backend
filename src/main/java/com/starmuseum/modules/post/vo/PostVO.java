package com.starmuseum.modules.post.vo;

import com.starmuseum.common.vo.LocationVO;
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

    /**
     * 阶段3.1：位置（列表也返回）
     */
    private LocationVO location;
}
