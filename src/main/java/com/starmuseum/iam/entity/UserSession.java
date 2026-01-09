package com.starmuseum.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * user_session：
 * - refresh token 会话（可撤销）
 * - DB 是“最终事实来源”，Redis 是“高速校验/TTL 管理”
 */
@TableName("user_session")
@Data
public class UserSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String refreshTokenHash;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

}
