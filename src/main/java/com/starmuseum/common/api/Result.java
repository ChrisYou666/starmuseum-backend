package com.starmuseum.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null, Instant.now().toEpochMilli());
    }

    public static <T> Result<T> fail(ResultCode code, String message, T data) {
        return new Result<>(code.getCode(), message, data, Instant.now().toEpochMilli());
    }
}
