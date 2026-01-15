package com.starmuseum.iam.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.iam.dto.UpdateAvatarRequest;
import com.starmuseum.iam.dto.UpdatePrivacyRequest;
import com.starmuseum.iam.dto.UpdateProfileRequest;
import com.starmuseum.iam.service.MeService;
import com.starmuseum.iam.vo.MeResponse;
import com.starmuseum.iam.vo.PrivacySettingResponse;
import com.starmuseum.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping
    public Result<MeResponse> me() {
        Long userId = CurrentUser.requireUserId();
        return Result.ok(meService.me(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UpdateProfileRequest req) {
        Long userId = CurrentUser.requireUserId();
        meService.updateProfile(userId, req);
        return Result.ok();
    }

    /**
     * 3.2 新增：获取隐私设置
     */
    @GetMapping("/privacy")
    public Result<PrivacySettingResponse> getPrivacy() {
        Long userId = CurrentUser.requireUserId();
        return Result.ok(meService.getPrivacy(userId));
    }

    /**
     * 3.2 修改：更新隐私设置，并返回更新后的结果
     */
    @PutMapping("/privacy")
    public Result<PrivacySettingResponse> updatePrivacy(@RequestBody @Valid UpdatePrivacyRequest req) {
        Long userId = CurrentUser.requireUserId();
        return Result.ok(meService.updatePrivacy(userId, req));
    }

    /**
     * 头像更新（推荐用 mediaId 方式：可控 + 可校验）
     * PUT /api/iam/me/avatar
     * body: { "mediaId": 123 }
     */
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody @Valid UpdateAvatarRequest req) {
        Long userId = CurrentUser.requireUserId();
        meService.updateAvatar(userId, req);
        return Result.ok();
    }
}
