package com.starmuseum.starmuseum.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一分页响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private long total;
    private long pages;
    private long pageNum;
    private long pageSize;
    private List<T> records;

    /**
     * 原有写法：从 MyBatis-Plus IPage 转换
     */
    public static <T> PageResponse<T> from(IPage<T> page) {
        return new PageResponse<>(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }

    /**
     * 新增别名：兼容 PageResponse.of(page) 写法
     */
    public static <T> PageResponse<T> of(IPage<T> page) {
        return from(page);
    }
}
