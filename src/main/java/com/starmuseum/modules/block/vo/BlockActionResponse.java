package com.starmuseum.modules.block.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockActionResponse {

    private Long blockedUserId;

    private LocalDateTime createdAt;
}
