// src/main/java/com/starmuseum/modules/astro/device/service/DeviceProfileService.java
package com.starmuseum.modules.astro.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starmuseum.modules.astro.device.dto.DeviceProfileCreateRequest;
import com.starmuseum.modules.astro.device.dto.DeviceProfileUpdateRequest;
import com.starmuseum.modules.astro.device.entity.DeviceProfile;
import com.starmuseum.modules.astro.device.vo.DeviceProfileVO;

import java.util.List;

public interface DeviceProfileService extends IService<DeviceProfile> {

    DeviceProfileVO createMy(Long userId, DeviceProfileCreateRequest req);

    DeviceProfileVO updateMy(Long userId, Long id, DeviceProfileUpdateRequest req);

    void deleteMy(Long userId, Long id);

    DeviceProfileVO getMy(Long userId, Long id);

    List<DeviceProfileVO> listMy(Long userId, String type);

    DeviceProfileVO setDefaultMy(Long userId, Long id);

    DeviceProfileVO getDefaultMy(Long userId, String type);
}
