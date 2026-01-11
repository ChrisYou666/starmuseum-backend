package com.starmuseum.modules.astro.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkySummaryResponse {

    private Meta meta;
    private List<StarPositionVO> items;

    @Data
    public static class Meta {
        private String catalogVersionCode;
        private String time; // 原样返回 ISO 字符串
        private Double lat;
        private Double lon;

        private Integer requestedLimit;
        private Boolean visibleOnly;

        // ✅ 新增：排序方式（mag / alt）
        private String sort;

        private Integer total; // items.size()
    }
}
