package com.starmuseum.modules.observation.vo;

import lombok.Data;

@Data
public class ObservationPublishResponse {
    private Long logId;
    private Long postId;
    private Boolean alreadyPublished;
}
