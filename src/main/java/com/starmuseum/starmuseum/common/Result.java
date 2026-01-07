package com.starmuseum.starmuseum.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    /**
     * 成功响应（原有写法）
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（无 data）
     */
    public static Result<Void> ok() {
        return ok(null);
    }

    /**
     * 失败响应（原有写法）
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（原有写法）
     */
    public static <T> Result<T> fail(ResultCode rc) {
        return fail(rc.getCode(), rc.getMessage());
    }

    // =========================
    // 新增：兼容常见命名（success / error）
    // 不影响现有 ok/fail 的任何使用
    // =========================

    /**
     * 成功响应（别名，兼容 Result.success(...) 写法）
     */
    public static <T> Result<T> success(T data) {
        return ok(data);
    }

    /**
     * 成功响应（无 data，别名）
     */
    public static Result<Void> success() {
        return ok();
    }

    /**
     * 失败响应（别名，兼容 Result.error(...) 写法）
     */
    public static <T> Result<T> error(int code, String message) {
        return fail(code, message);
    }

    /**
     * 失败响应（别名）
     */
    public static <T> Result<T> error(ResultCode rc) {
        return fail(rc);
    }
}