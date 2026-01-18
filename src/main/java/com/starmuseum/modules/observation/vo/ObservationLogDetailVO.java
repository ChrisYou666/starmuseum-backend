package com.starmuseum.modules.observation.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ObservationLogDetailVO {

    private Long id;
    private Long userId;

    private LocalDateTime observedAt;
    private String method;
    private Long deviceProfileId;

    private String notes;
    private Integer success;
    private Integer rating;

    private String locationVisibility;
    private Double locationLat;
    private Double locationLon;
    private Double locationLatFuzzy;
    private Double locationLonFuzzy;
    private String locationCity;

    private Integer published;
    private Long postId;

    private List<ObservationTargetVO> targets;
    private List<ObservationMediaVO> mediaList;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
