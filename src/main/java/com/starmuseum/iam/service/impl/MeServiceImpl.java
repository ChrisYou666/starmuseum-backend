package com.starmuseum.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.enums.ExactLocationPublicStrategy;
import com.starmuseum.common.enums.LocationVisibility;
import com.starmuseum.iam.dto.UpdateAvatarRequest;
import com.starmuseum.iam.dto.UpdatePrivacyRequest;
import com.starmuseum.iam.dto.UpdateProfileRequest;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.entity.UserPrivacySetting;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.iam.mapper.UserPrivacySettingMapper;
import com.starmuseum.iam.service.MeService;
import com.starmuseum.iam.vo.MeResponse;
import com.starmuseum.iam.vo.PrivacySettingResponse;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.mapper.MediaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

        u.setNickname(req.getNickname());
        u.setAvatarUrl(req.getAvatarUrl());
        u.setBio(req.getBio());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public PrivacySettingResponse getPrivacy(Long userId) {
        UserPrivacySetting p = privacySettingMapper.selectById(userId);
        if (p == null) {
            p = buildDefaultPrivacySetting(userId);
            privacySettingMapper.insert(p);
        } else {
            // 兜底：老数据可能为空（企业升级很常见）
            boolean changed = false;
            if (!StringUtils.hasText(p.getPostVisibilityDefault())) {
                p.setPostVisibilityDefault("PUBLIC");
                changed = true;
            }
            if (!StringUtils.hasText(p.getDefaultLocationVisibility())) {
                p.setDefaultLocationVisibility(LocationVisibility.HIDDEN.name());
                changed = true;
            }
            if (!StringUtils.hasText(p.getExactLocationPublicStrategy())) {
                p.setExactLocationPublicStrategy(ExactLocationPublicStrategy.FUZZY.name());
                changed = true;
            }
            if (changed) {
                p.setUpdatedAt(LocalDateTime.now());
                privacySettingMapper.updateById(p);
            }
        }

        return toResponse(p);
    }

    @Override
    @Transactional
    public PrivacySettingResponse updatePrivacy(Long userId, UpdatePrivacyRequest req) {
        UserPrivacySetting p = privacySettingMapper.selectById(userId);
        if (p == null) {
            p = buildDefaultPrivacySetting(userId);
            privacySettingMapper.insert(p);
        }

        boolean changed = false;

        // 1) postVisibilityDefault
        if (StringUtils.hasText(req.getPostVisibilityDefault())) {
            String v = req.getPostVisibilityDefault().trim().toUpperCase();
            if (!("PUBLIC".equals(v) || "PRIVATE".equals(v) || "FOLLOWERS".equals(v))) {
                throw new IllegalArgumentException("Invalid postVisibilityDefault: " + req.getPostVisibilityDefault());
            }
            p.setPostVisibilityDefault(v);
            changed = true;
        }

        // 2) defaultLocationVisibility
        if (StringUtils.hasText(req.getDefaultLocationVisibility())) {
            String v = req.getDefaultLocationVisibility().trim().toUpperCase();
            try {
                LocationVisibility.valueOf(v);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid defaultLocationVisibility: " + req.getDefaultLocationVisibility());
            }
            p.setDefaultLocationVisibility(v);
            changed = true;
        }

        // 3) exactLocationPublicStrategy
        if (StringUtils.hasText(req.getExactLocationPublicStrategy())) {
            String v = req.getExactLocationPublicStrategy().trim().toUpperCase();
            try {
                ExactLocationPublicStrategy.valueOf(v);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid exactLocationPublicStrategy: " + req.getExactLocationPublicStrategy());
            }
            p.setExactLocationPublicStrategy(v);
            changed = true;
        }

        if (changed) {
            p.setUpdatedAt(LocalDateTime.now());
            privacySettingMapper.updateById(p);
        }

        return toResponse(p);
    }

    private UserPrivacySetting buildDefaultPrivacySetting(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        UserPrivacySetting p = new UserPrivacySetting();
        p.setUserId(userId);
        p.setPostVisibilityDefault("PUBLIC");
        p.setDefaultLocationVisibility(LocationVisibility.HIDDEN.name());
        p.setExactLocationPublicStrategy(ExactLocationPublicStrategy.FUZZY.name());
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return p;
    }

    private PrivacySettingResponse toResponse(UserPrivacySetting p) {
        PrivacySettingResponse r = new PrivacySettingResponse();
        r.setUserId(p.getUserId());
        r.setPostVisibilityDefault(p.getPostVisibilityDefault());
        r.setDefaultLocationVisibility(p.getDefaultLocationVisibility());
        r.setExactLocationPublicStrategy(p.getExactLocationPublicStrategy());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
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

        if (!userId.equals(media.getUserId())) {
            throw new IllegalArgumentException("无权使用他人的头像资源");
        }

        if (!MediaBizType.AVATAR.name().equals(media.getBizType())) {
            throw new IllegalArgumentException("该 media 不是 AVATAR 类型，不能设置为头像");
        }

        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }

        u.setAvatarUrl(media.getOriginUrl());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }
}
