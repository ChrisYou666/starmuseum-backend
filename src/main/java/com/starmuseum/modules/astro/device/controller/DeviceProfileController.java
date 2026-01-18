// src/main/java/com/starmuseum/modules/astro/device/controller/DeviceProfileController.java
package com.starmuseum.modules.astro.device.controller;

import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.astro.device.dto.DeviceProfileCreateRequest;
import com.starmuseum.modules.astro.device.dto.DeviceProfileUpdateRequest;
import com.starmuseum.modules.astro.device.service.DeviceProfileService;
import com.starmuseum.modules.astro.device.vo.DeviceProfileVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/astro/device-profiles")
public class DeviceProfileController {

    private final DeviceProfileService deviceProfileService;

    public DeviceProfileController(DeviceProfileService deviceProfileService) {
        this.deviceProfileService = deviceProfileService;
    }

    /**
     * 新建设备配置
     */
    @PostMapping
    public DeviceProfileVO create(@Valid @RequestBody DeviceProfileCreateRequest req) {
        Long userId = CurrentUser.requireUserId();
        return deviceProfileService.createMy(userId, req);
    }

    /**
     * 修改设备配置（type 不允许修改）
     */
    @PutMapping("/{id}")
    public DeviceProfileVO update(@PathVariable Long id, @Valid @RequestBody DeviceProfileUpdateRequest req) {
        Long userId = CurrentUser.requireUserId();
        return deviceProfileService.updateMy(userId, id, req);
    }

    /**
     * 删除设备配置（幂等）
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long userId = CurrentUser.requireUserId();
        deviceProfileService.deleteMy(userId, id);
    }

    /**
     * 设备配置详情
     */
    @GetMapping("/{id}")
    public DeviceProfileVO detail(@PathVariable Long id) {
        Long userId = CurrentUser.requireUserId();
        return deviceProfileService.getMy(userId, id);
    }

    /**
     * 我的设备配置列表
     * - type 可选：PHOTO / VISUAL
     */
    @GetMapping
    public List<DeviceProfileVO> list(@RequestParam(required = false) String type) {
        Long userId = CurrentUser.requireUserId();
        return deviceProfileService.listMy(userId, type);
    }

    /**
     * 获取某类型的默认设备
     */
    @GetMapping("/default")
    public DeviceProfileVO getDefault(@RequestParam @NotBlank(message = "type 不能为空") String type) {
        Long userId = CurrentUser.requireUserId();
        return deviceProfileService.getDefaultMy(userId, type);
    }

    /**
     * 设置为默认（同 user+type 只能有一个 default）
     */
    @PostMapping("/{id}/default")
    public DeviceProfileVO setDefault(@PathVariable Long id) {
        Long userId = CurrentUser.requireUserId();
        return deviceProfileService.setDefaultMy(userId, id);
    }
}
