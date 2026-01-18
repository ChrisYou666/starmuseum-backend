package com.starmuseum.modules.observation.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ObservationLogVO {

    private Long id;
    private LocalDateTime observedAt;
    private String method;
    private Long deviceProfileId;

    private Integer success;
    private Integer rating;

    private String locationVisibility;
    private String locationCity;

    private Integer published;
    private Long postId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
