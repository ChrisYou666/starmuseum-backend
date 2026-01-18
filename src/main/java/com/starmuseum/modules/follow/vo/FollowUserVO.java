package com.starmuseum.modules.follow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserVO {

    private Long userId;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime followedAt;
}
