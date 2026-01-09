package com.starmuseum.iam.service;

import com.starmuseum.iam.dto.UpdateAvatarRequest;
import com.starmuseum.iam.dto.UpdatePrivacyRequest;
import com.starmuseum.iam.dto.UpdateProfileRequest;
import com.starmuseum.iam.vo.MeResponse;

public interface MeService {

    MeResponse me(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest req);

    void updatePrivacy(Long userId, UpdatePrivacyRequest req);

    /**
     * 通过 mediaId 更新头像（需要校验 media 属于本人，且 bizType=AVATAR）
     */
    void updateAvatar(Long userId, UpdateAvatarRequest req);
}
