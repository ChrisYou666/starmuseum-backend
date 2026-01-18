package com.starmuseum.common.exception;

import com.starmuseum.common.api.Result;
import com.starmuseum.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------
    // 0) BizException（业务异常）
    // 目标：坏包校验等“可预期错误”，必须返回 4xx + 明确 message，而不是 500
    // -------------------------
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleBizException(
        BizException ex,
        HttpServletRequest request
    ) {
        int status = normalizeHttpStatus(ex.getHttpStatus());

        ApiErrorResponse body = ApiErrorResponse.of(
            status,
            "Business Error",
            Objects.toString(ex.getMessage(), "请求失败"),
            request.getRequestURI()
        );
        body.setTimestamp(OffsetDateTime.now().toString());

        // 如果业务异常给了 5xx（理论上不常见），打印堆栈方便定位
        if (status >= 500) {
            log.error("BizException status>=500, uri={}", request.getRequestURI(), ex);
        }

        return ResponseEntity
            .status(status)
            .body(Result.fail(mapResultCode(status), body.getDetail(), body));
    }

    // -------------------------
    // 1) Validation errors
    // -------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), Objects.toString(fe.getDefaultMessage(), "参数不合法"));
        }

        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "请求参数不合法",
            request.getRequestURI()
        );
        body.setErrors(errors);
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Result.fail(ResultCode.BAD_REQUEST, "请求参数不合法", body));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleBindException(
        BindException ex,
        HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), Objects.toString(fe.getDefaultMessage(), "参数不合法"));
        }

        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "请求参数不合法",
            request.getRequestURI()
        );
        body.setErrors(errors);
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Result.fail(ResultCode.BAD_REQUEST, "请求参数不合法", body));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleConstraintViolationException(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.put(v.getPropertyPath().toString(), Objects.toString(v.getMessage(), "参数不合法"));
        }

        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "请求参数不合法",
            request.getRequestURI()
        );
        body.setErrors(errors);
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Result.fail(ResultCode.BAD_REQUEST, "请求参数不合法", body));
    }

    // -------------------------
    // 2) Time parse
    // -------------------------

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleDateTimeParseException(
        DateTimeParseException ex,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Time Parse Failed",
            "time 格式不正确。请传 ISO-8601 且带时区，例如：2026-01-10T12:00:00Z",
            request.getRequestURI()
        );
        body.setInvalidTime(ex.getParsedString());
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Result.fail(ResultCode.BAD_REQUEST, body.getDetail(), body));
    }

    // -------------------------
    // 3) Param type mismatch
    // -------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Type Mismatch",
            "参数类型不正确: " + ex.getName(),
            request.getRequestURI()
        );
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Result.fail(ResultCode.BAD_REQUEST, body.getDetail(), body));
    }

    // -------------------------
    // 4) Spring Web / 404 static
    // -------------------------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleNoResourceFound(
        NoResourceFoundException ex,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            "资源不存在",
            request.getRequestURI()
        );
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Result.fail(ResultCode.NOT_FOUND, body.getDetail(), body));
    }

    // -------------------------
    // 5) ResponseStatus / ErrorResponseException
    // -------------------------

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleResponseStatusException(
        ResponseStatusException ex,
        HttpServletRequest request
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();
        ApiErrorResponse body = ApiErrorResponse.of(
            statusCode.value(),
            "Request Failed",
            Objects.toString(ex.getReason(), "请求失败"),
            request.getRequestURI()
        );
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(statusCode)
            .body(Result.fail(mapResultCode(statusCode), body.getDetail(), body));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleErrorResponseException(
        ErrorResponseException ex,
        HttpServletRequest request
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();

        String detail = null;
        if (ex.getBody() != null) {
            detail = ex.getBody().getDetail();
        }
        if (detail == null || detail.isBlank()) {
            detail = Objects.toString(ex.getMessage(), "请求失败");
        }

        ApiErrorResponse body = ApiErrorResponse.of(
            statusCode.value(),
            "Request Failed",
            detail,
            request.getRequestURI()
        );
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(statusCode)
            .body(Result.fail(mapResultCode(statusCode), body.getDetail(), body));
    }

    // -------------------------
    // 6) Fallback
    // -------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<ApiErrorResponse>> handleException(
        Exception ex,
        HttpServletRequest request
    ) {
        // 未知异常必须打印堆栈，否则你会遇到“返回500但控制台没信息”的困境
        log.error("Unhandled exception, uri={}", request.getRequestURI(), ex);

        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Error",
            "服务器内部错误",
            request.getRequestURI()
        );
        body.setTimestamp(OffsetDateTime.now().toString());

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.fail(ResultCode.INTERNAL_ERROR, body.getDetail(), body));
    }

    private ResultCode mapResultCode(HttpStatusCode statusCode) {
        return mapResultCode(statusCode.value());
    }

    private ResultCode mapResultCode(int v) {
        if (v == 400) return ResultCode.BAD_REQUEST;
        if (v == 401) return ResultCode.UNAUTHORIZED;
        if (v == 403) return ResultCode.FORBIDDEN;
        if (v == 404) return ResultCode.NOT_FOUND;
        if (v == 405) return ResultCode.METHOD_NOT_ALLOWED;
        return ResultCode.INTERNAL_ERROR;
    }

    private int normalizeHttpStatus(int code) {
        // 合法区间 100-599 才当作 http status
        if (code >= 100 && code <= 599) return code;
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
