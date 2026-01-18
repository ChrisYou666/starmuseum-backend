package com.starmuseum.common.kv.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_kv_config")
public class SysKvConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey;
    private String configValue;

    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
