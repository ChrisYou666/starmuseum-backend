package com.starmuseum.iam.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.iam.dto.UpdateAvatarRequest;
import com.starmuseum.iam.dto.UpdatePrivacyRequest;
import com.starmuseum.iam.dto.UpdateProfileRequest;
import com.starmuseum.iam.service.MeService;
import com.starmuseum.iam.vo.MeResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping
    public Result<MeResponse> me(@AuthenticationPrincipal Long userId) {
        return Result.ok(meService.me(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal Long userId,
                                      @RequestBody @Valid UpdateProfileRequest req) {
        meService.updateProfile(userId, req);
        return Result.ok();
    }

    @PutMapping("/privacy")
    public Result<Void> updatePrivacy(@AuthenticationPrincipal Long userId,
                                      @RequestBody @Valid UpdatePrivacyRequest req) {
        meService.updatePrivacy(userId, req);
        return Result.ok();
    }

    /**
     * 头像更新（推荐用 mediaId 方式：可控 + 可校验）
     * PUT /api/iam/me/avatar
     * body: { "mediaId": 123 }
     */
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@AuthenticationPrincipal Long userId,
                                     @RequestBody @Valid UpdateAvatarRequest req) {
        meService.updateAvatar(userId, req);
        return Result.ok();
    }
}
