package com.starmuseum.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * application-dev.yml 示例：
 * app:
 *   admin:
 *     user-ids: [1]
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    /**
     * 管理员用户ID白名单
     */
    private List<Long> userIds = new ArrayList<>();
}
