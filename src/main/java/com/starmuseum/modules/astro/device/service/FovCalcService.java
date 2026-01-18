// src/main/java/com/starmuseum/modules/astro/device/service/FovCalcService.java
package com.starmuseum.modules.astro.device.service;

import com.starmuseum.modules.astro.device.dto.FovRequest;
import com.starmuseum.modules.astro.device.dto.FovResponse;

public interface FovCalcService {

    /**
     * 按请求计算 FOV
     */
    FovResponse calc(FovRequest req, Long userId);
}
