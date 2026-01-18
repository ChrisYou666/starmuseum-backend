package com.starmuseum.modules.observation.vo;

import lombok.Data;

@Data
public class ObservationMediaVO {

    private Long id;

    // 适配你现有 Media 字段
    private String originUrl;
    private String thumbUrl;
    private String mediumUrl;

    private String mimeType;

    private Integer width;
    private Integer height;
    private Long sizeBytes;

    private Integer sortOrder;
}
