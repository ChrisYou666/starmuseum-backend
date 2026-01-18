package com.starmuseum.modules.observation.service;

import com.starmuseum.modules.observation.dto.ObservationPublishRequest;
import com.starmuseum.modules.observation.vo.ObservationPublishResponse;

public interface ObservationPublishService {

    ObservationPublishResponse publishMy(Long userId, Long logId, ObservationPublishRequest req);
}
