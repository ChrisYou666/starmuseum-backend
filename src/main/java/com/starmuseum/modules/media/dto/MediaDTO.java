package com.starmuseum.modules.media.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class MediaDTO {

    private Long id;
    private Long userId;
    private String bizType;

    private String originUrl;
    private String thumbUrl;
    private String mediumUrl;

    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;

    private LocalDateTime createdAt;
}
