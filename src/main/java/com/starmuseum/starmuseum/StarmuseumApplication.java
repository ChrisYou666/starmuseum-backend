package com.starmuseum.starmuseum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.starmuseum.starmuseum.**.mapper")
@SpringBootApplication
public class StarmuseumApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarmuseumApplication.class, args);
    }
}