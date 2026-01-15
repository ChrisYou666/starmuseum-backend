package com.starmuseum.modules.block.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_block")
public class UserBlock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 拉黑发起者
     */
    private Long userId;

    /**
     * 被拉黑者
     */
    private Long blockedUserId;

    private LocalDateTime createdAt;
}
