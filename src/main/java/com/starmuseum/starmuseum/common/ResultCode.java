package com.starmuseum.starmuseum.common;

import lombok.Getter;

/**
 * 统一返回码（你后续可以继续扩展）
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(400, "参数错误"),
    NOT_FOUND(404, "数据不存在"),
    BIZ_ERROR(1000, "业务异常"),
    INTERNAL_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}