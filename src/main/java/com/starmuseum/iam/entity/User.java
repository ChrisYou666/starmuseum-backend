package com.starmuseum.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * user 表实体：
 * - 存账号信息与基础资料
 */
@TableName("user")
@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    /**
     * 数据库字段：password_hash
     * MyBatis-Plus 默认会做下划线映射（passwordHash <-> password_hash）
     */
    private String passwordHash;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
