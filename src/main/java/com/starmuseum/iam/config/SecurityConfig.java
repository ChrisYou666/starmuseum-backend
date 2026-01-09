package com.starmuseum.iam.config;

import com.starmuseum.iam.security.JwtAuthenticationFilter;
import com.starmuseum.iam.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security 配置要点：
 * 1) 我们是前后端分离，使用 JWT，所以 Session 关闭（STATELESS）
 * 2) 放行 register/login/refresh
 * 3) 其它 /api/** 默认需要登录
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtUtil jwtUtil(SecurityProperties props) {
        return new JwtUtil(props);
    }

    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                           JwtUtil jwtUtil) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
            // 认证接口放行
            .requestMatchers("/api/iam/auth/register").permitAll()
            .requestMatchers("/api/iam/auth/login").permitAll()
            .requestMatchers("/api/iam/auth/refresh").permitAll()

            // 健康检查/静态资源（按你项目情况可删减）
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/", "/index.html", "/favicon.ico").permitAll()

            // 其它 API 默认需要登录
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
        );

        // JWT 过滤器放在 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码加密器：BCrypt
     * - 注册时 encode
     * - 登录时 matches
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
