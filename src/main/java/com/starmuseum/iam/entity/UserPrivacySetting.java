package com.starmuseum.iam.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * user_privacy_setting 表实体：
 * - 与 user 一对一（user_id 既是主键也是外键）
 */
@TableName("user_privacy_setting")
@Data
public class UserPrivacySetting {

    @TableId
    private Long userId;

    /**
     * 默认发帖可见性：PUBLIC / PRIVATE / FOLLOWERS(预留)
     */
    private String postVisibilityDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
