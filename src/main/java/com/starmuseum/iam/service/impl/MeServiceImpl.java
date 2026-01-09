package com.starmuseum.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.iam.dto.UpdateAvatarRequest;
import com.starmuseum.iam.dto.UpdatePrivacyRequest;
import com.starmuseum.iam.dto.UpdateProfileRequest;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.entity.UserPrivacySetting;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.iam.mapper.UserPrivacySettingMapper;
import com.starmuseum.iam.service.MeService;
import com.starmuseum.iam.vo.MeResponse;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.mapper.MediaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MeServiceImpl implements MeService {

    private final UserMapper userMapper;
    private final UserPrivacySettingMapper privacySettingMapper;
    private final MediaMapper mediaMapper;

    public MeServiceImpl(UserMapper userMapper,
                         UserPrivacySettingMapper privacySettingMapper,
                         MediaMapper mediaMapper) {
        this.userMapper = userMapper;
        this.privacySettingMapper = privacySettingMapper;
        this.mediaMapper = mediaMapper;
    }

    @Override
    public MeResponse me(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }

        UserPrivacySetting p = privacySettingMapper.selectOne(
            new LambdaQueryWrapper<UserPrivacySetting>()
                .eq(UserPrivacySetting::getUserId, userId)
                .last("LIMIT 1")
        );

        MeResponse resp = new MeResponse();
        resp.setId(u.getId());
        resp.setEmail(u.getEmail());
        resp.setNickname(u.getNickname());
        resp.setAvatarUrl(u.getAvatarUrl());
        resp.setBio(u.getBio());
        resp.setStatus(u.getStatus());

        if (p != null) {
            resp.setPostVisibilityDefault(p.getPostVisibilityDefault());
        }

        return resp;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest req) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }

        // 只更新基本资料（不校验 media）
        u.setNickname(req.getNickname());
        u.setAvatarUrl(req.getAvatarUrl());
        u.setBio(req.getBio());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public void updatePrivacy(Long userId, UpdatePrivacyRequest req) {
        UserPrivacySetting p = privacySettingMapper.selectOne(
            new LambdaQueryWrapper<UserPrivacySetting>()
                .eq(UserPrivacySetting::getUserId, userId)
                .last("LIMIT 1")
        );

        if (p == null) {
            p = new UserPrivacySetting();
            p.setUserId(userId);
            p.setPostVisibilityDefault(req.getPostVisibilityDefault());
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            privacySettingMapper.insert(p);
            return;
        }

        p.setPostVisibilityDefault(req.getPostVisibilityDefault());
        p.setUpdatedAt(LocalDateTime.now());
        privacySettingMapper.updateById(p);
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, UpdateAvatarRequest req) {
        Long mediaId = req.getMediaId();
        if (mediaId == null) {
            throw new IllegalArgumentException("mediaId is null");
        }

        Media media = mediaMapper.selectById(mediaId);
        if (media == null) {
            throw new IllegalArgumentException("media not found: " + mediaId);
        }

        // 只能用自己的 media
        if (!userId.equals(media.getUserId())) {
            throw new IllegalArgumentException("无权使用他人的头像资源");
        }

        // bizType 必须是 AVATAR
        if (!MediaBizType.AVATAR.name().equals(media.getBizType())) {
            throw new IllegalArgumentException("该 media 不是 AVATAR 类型，不能设置为头像");
        }

        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }

        // 用原图当头像（你也可以改成 media.getMediumUrl()）
        u.setAvatarUrl(media.getOriginUrl());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }
}
