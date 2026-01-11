package com.starmuseum.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一错误返回结构（给前端用）
 *
 * 参考格式：
 * {
 *   "type": "about:blank",
 *   "title": "Validation Failed",
 *   "status": 400,
 *   "detail": "请求参数不合法",
 *   "instance": "/api/astro/body/1",
 *   "errors": {
 *     "detail.lat": "最大不能超过90"
 *   },
 *   "invalidTime": "2026-01-10T12:00:00",
 *   "timestamp": "2026-01-11T01:16:32.210+08:00"
 * }
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * RFC7807 风格字段（这里用 about:blank）
     */
    private String type;

    /**
     * 错误标题（给人看的短标题）
     */
    private String title;

    /**
     * HTTP 状态码
     */
    private Integer status;

    /**
     * 详细描述（给人看的可读信息）
     */
    private String detail;

    /**
     * 请求路径
     */
    private String instance;

    /**
     * 字段级错误（key=字段路径，value=错误信息）
     */
    private Map<String, String> errors;

    /**
     * time 解析失败时，返回原始传入值（便于排查）
     */
    private String invalidTime;

    /**
     * 时间戳
     */
    private String timestamp;

    public static ApiErrorResponse of(int status, String title, String detail, String instance) {
        ApiErrorResponse r = new ApiErrorResponse();
        r.setType("about:blank");
        r.setStatus(status);
        r.setTitle(title);
        r.setDetail(detail);
        r.setInstance(instance);
        r.setTimestamp(OffsetDateTime.now().toString());
        return r;
    }

    public ApiErrorResponse addError(String field, String message) {
        if (this.errors == null) {
            this.errors = new LinkedHashMap<>();
        }
        this.errors.put(field, message);
        return this;
    }
}
