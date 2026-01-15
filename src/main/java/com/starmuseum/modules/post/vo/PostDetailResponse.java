package com.starmuseum.modules.post.vo;

import com.starmuseum.common.vo.LocationVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情返回
 */
@Data
public class PostDetailResponse {

    private Long id;

    private Long userId;
    private String nickname;
    private String avatarUrl;

    private String content;
    private String visibility;

    private Integer likeCount;

    /**
     * 评论总数（建议用实时统计，避免 post 表字段不同步）
     */
    private Integer commentCount;

    /**
     * 详情页直接返回最新 N 条评论（例如 3 条）
     * 完整列表走：GET /api/post/{postId}/comment/page
     */
    private List<PostCommentResponse> latestComments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PostMediaItem> mediaList;

    /**
     * 当前登录用户是否已点赞该帖（未登录/不传uid时为 false）
     */
    private Boolean likedByMe;

    /**
     * 阶段3.1：位置（按隐私规则过滤后的结果）
     * - HIDDEN：null
     * - CITY：仅 cityName
     * - FUZZY：fuzzy 坐标
     * - EXACT：仅作者可见，否则降级为 FUZZY（阶段3固定策略）
     */
    private LocationVO location;
}
