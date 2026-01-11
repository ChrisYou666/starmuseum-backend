package com.starmuseum.common.api;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "success"),

    BAD_REQUEST(40000, "bad request"),
    VALIDATION_ERROR(40001, "validation error"),
    UNAUTHORIZED(40100, "unauthorized"),
    FORBIDDEN(40300, "forbidden"),
    NOT_FOUND(40400, "not found"),
    METHOD_NOT_ALLOWED(40500, "method not allowed"),

    BUSINESS_ERROR(50001, "business error"),
    INTERNAL_ERROR(50000, "internal server error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
