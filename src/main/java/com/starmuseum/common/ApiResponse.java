package com.starmuseum.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 统一成功响应包装（方案A）
 * 约定：
 * - code = 0 表示成功
 * - message = "OK"
 * - data = 业务数据
 * - timestamp = 响应生成时间（带时区）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;

    private String message;

    private T data;

    private OffsetDateTime timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data, OffsetDateTime.now());
    }
}
