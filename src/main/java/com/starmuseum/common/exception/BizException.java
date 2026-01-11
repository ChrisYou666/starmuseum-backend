package com.starmuseum.common.exception;

/**
 * 业务异常（你后续可以在 Service 里用 throw new BizException(...)）
 */
public class BizException extends RuntimeException {

    private final int httpStatus;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
