package com.starmuseum.modules.catalog.dto;

import lombok.Data;

/**
 * 回滚请求：
 * - targetCode 为空：自动回滚到“上一激活版本”
 * - targetCode 非空：回滚到指定版本
 */
@Data
public class CatalogRollbackRequest {
    private String targetCode;
}
