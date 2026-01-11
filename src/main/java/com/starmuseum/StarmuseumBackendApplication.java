package com.starmuseum;

import com.starmuseum.common.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({CorsProperties.class})
@SpringBootApplication(scanBasePackages = "com.starmuseum")
public class StarmuseumBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarmuseumBackendApplication.class, args);
    }
}
