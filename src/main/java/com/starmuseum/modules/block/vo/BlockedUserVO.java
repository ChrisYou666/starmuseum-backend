package com.starmuseum.modules.block.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockedUserVO {

    private Long blockedUserId;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;
}
