package com.starmuseum.common;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局成功响应包装器：
 * - 只包装“成功响应”（即 Controller 正常返回的 body）
 * - 不影响异常处理（异常依然走你的 ProblemDetail / 统一错误返回）
 * - 已经是 ApiResponse 的不重复包装
 * - swagger / actuator 等路径不包装，避免影响工具
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        String path = request.getURI().getPath();

        // 不包装这些路径：swagger / api-docs / actuator
        if (path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/actuator")) {
            return body;
        }

        // 错误响应（ProblemDetail）不要包装
        if (body instanceof ProblemDetail) {
            return body;
        }

        // 已经是 ApiResponse 的不要重复包装
        if (body instanceof ApiResponse) {
            return body;
        }

        // 二进制/资源响应不要包装
        if (body instanceof byte[] || body instanceof Resource) {
            return body;
        }

        // 如果是纯文本响应（一般你们接口不会这样），不包装，避免 StringHttpMessageConverter 出错
        if (selectedContentType != null
            && (MediaType.TEXT_PLAIN.includes(selectedContentType)
            || MediaType.TEXT_HTML.includes(selectedContentType))) {
            return body;
        }

        // body 可能为 null，也统一返回成功结构
        return ApiResponse.ok(body);
    }
}
