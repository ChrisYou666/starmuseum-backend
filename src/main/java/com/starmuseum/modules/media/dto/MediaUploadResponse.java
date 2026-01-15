package com.starmuseum.modules.media.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MediaUploadResponse {

    private Long id;
    private String bizType;

    private String originUrl;
    private String thumbUrl;
    private String mediumUrl;

    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;

    private Boolean exifStripped;
    private Boolean exifHasGps;
    private Boolean exifHasDevice;

    private LocalDateTime createdAt;
}
