// src/main/java/com/starmuseum/modules/astro/device/controller/FovController.java
package com.starmuseum.modules.astro.device.controller;

import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.astro.device.dto.FovRequest;
import com.starmuseum.modules.astro.device.dto.FovResponse;
import com.starmuseum.modules.astro.device.service.FovCalcService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/astro/fov")
public class FovController {

    private final FovCalcService fovCalcService;

    public FovController(FovCalcService fovCalcService) {
        this.fovCalcService = fovCalcService;
    }

    /**
     * 计算 FOV
     * - 若 body 里传 profileId，则以 profile 为准
     * - 否则需要传 type + 对应参数
     */
    @PostMapping("/calc")
    public FovResponse calc(@Valid @RequestBody FovRequest req) {
        Long userId = CurrentUser.requireUserId();
        return fovCalcService.calc(req, userId);
    }

    /**
     * 便捷接口：按 profileId 计算（rotationDeg 可选）
     */
    @GetMapping("/calc/by-profile/{profileId}")
    public FovResponse calcByProfile(@PathVariable Long profileId,
                                     @RequestParam(required = false)
                                     @Min(value = -180, message = "rotationDeg 最小 -180")
                                     @Max(value = 180, message = "rotationDeg 最大 180")
                                     Double rotationDeg) {
        Long userId = CurrentUser.requireUserId();
        FovRequest req = new FovRequest();
        req.setProfileId(profileId);
        req.setRotationDeg(rotationDeg);
        return fovCalcService.calc(req, userId);
    }
}
