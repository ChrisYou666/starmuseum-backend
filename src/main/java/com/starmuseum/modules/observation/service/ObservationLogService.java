package com.starmuseum.modules.observation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.observation.dto.ObservationLogCreateRequest;
import com.starmuseum.modules.observation.dto.ObservationLogUpdateRequest;
import com.starmuseum.modules.observation.vo.ObservationLogDetailVO;
import com.starmuseum.modules.observation.vo.ObservationLogVO;

public interface ObservationLogService {

    ObservationLogDetailVO createMy(Long userId, ObservationLogCreateRequest req);

    ObservationLogDetailVO updateMy(Long userId, Long logId, ObservationLogUpdateRequest req);

    IPage<ObservationLogVO> pageMy(Long userId, int page, int size);

    ObservationLogDetailVO getMy(Long userId, Long logId);

    void deleteMy(Long userId, Long logId);
}
