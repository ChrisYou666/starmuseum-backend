package com.starmuseum.iam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 把 application.yml 的 starmuseum.security 配置映射成 Java 对象。
 */
@Configuration
@ConfigurationProperties(prefix = "starmuseum.security")
@Data
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Refresh refresh = new Refresh();

    @Data
    public static class Jwt {
        private String issuer;
        private String secret;
        private int accessTokenTtlMinutes;
    }

    @Data
    public static class Refresh {
        private int ttlDays;
        private String pepper;
    }
}
