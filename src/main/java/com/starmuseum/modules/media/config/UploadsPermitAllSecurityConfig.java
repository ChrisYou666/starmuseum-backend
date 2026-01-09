package com.starmuseum.modules.media.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 专门给 /uploads/** 放行（permitAll）
 * 这样浏览器/前端加载图片时不需要 token，也不需要你去改现有 SecurityConfig。
 *
 * 注意：@Order(0) 确保它优先匹配 /uploads/**
 */
@Configuration
public class UploadsPermitAllSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain uploadsChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/uploads/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults());
        return http.build();
    }
}
