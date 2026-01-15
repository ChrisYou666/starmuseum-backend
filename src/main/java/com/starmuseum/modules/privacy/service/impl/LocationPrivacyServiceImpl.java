package com.starmuseum.modules.privacy.service.impl;

import com.starmuseum.common.enums.ExactLocationPublicStrategy;
import com.starmuseum.common.enums.LocationVisibility;
import com.starmuseum.common.vo.LocationVO;
import com.starmuseum.iam.entity.UserPrivacySetting;
import com.starmuseum.iam.mapper.UserPrivacySettingMapper;
import com.starmuseum.modules.post.entity.Post;
import com.starmuseum.modules.privacy.service.LocationPrivacyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class LocationPrivacyServiceImpl implements LocationPrivacyService {

    private final UserPrivacySettingMapper privacySettingMapper;

    public LocationPrivacyServiceImpl(UserPrivacySettingMapper privacySettingMapper) {
        this.privacySettingMapper = privacySettingMapper;
    }

    @Override
    public LocationVO buildLocationForViewer(Post post, Long viewerId) {
        if (post == null) return null;

        LocationVisibility visibility = parseVisibility(post.getLocationVisibility());
        Long authorId = post.getUserId();

        // 未设置或 HIDDEN
        if (visibility == null || visibility == LocationVisibility.HIDDEN) {
            return null;
        }

        boolean isOwner = (viewerId != null && viewerId.equals(authorId));

        // CITY
        if (visibility == LocationVisibility.CITY) {
            LocationVO vo = new LocationVO();
            vo.setVisibility(LocationVisibility.CITY);
            vo.setCityName(post.getLocationCity());
            return vo;
        }

        // FUZZY
        if (visibility == LocationVisibility.FUZZY) {
            return buildFuzzy(post);
        }

        // EXACT：本人拿精确；他人按“作者策略”降级（3.2）
        if (visibility == LocationVisibility.EXACT) {
            if (isOwner) {
                LocationVO vo = new LocationVO();
                vo.setVisibility(LocationVisibility.EXACT);
                vo.setCityName(post.getLocationCity());
                vo.setLat(toDouble(post.getLocationLat()));
                vo.setLon(toDouble(post.getLocationLon()));
                return vo;
            }

            ExactLocationPublicStrategy strategy = resolveExactStrategy(authorId);

            if (strategy == ExactLocationPublicStrategy.CITY) {
                LocationVO vo = new LocationVO();
                vo.setVisibility(LocationVisibility.CITY);
                vo.setCityName(post.getLocationCity());
                return vo;
            }

            // 默认 FUZZY
            return buildFuzzy(post);
        }

        return null;
    }

    private LocationVO buildFuzzy(Post post) {
        LocationVO vo = new LocationVO();
        vo.setVisibility(LocationVisibility.FUZZY);
        vo.setCityName(post.getLocationCity());
        vo.setLat(toDouble(post.getLocationLatFuzzy()));
        vo.setLon(toDouble(post.getLocationLonFuzzy()));
        return vo;
    }

    private ExactLocationPublicStrategy resolveExactStrategy(Long authorId) {
        UserPrivacySetting s = privacySettingMapper.selectById(authorId);
        if (s == null || !StringUtils.hasText(s.getExactLocationPublicStrategy())) {
            return ExactLocationPublicStrategy.FUZZY;
        }
        try {
            return ExactLocationPublicStrategy.valueOf(s.getExactLocationPublicStrategy().trim().toUpperCase());
        } catch (Exception e) {
            return ExactLocationPublicStrategy.FUZZY;
        }
    }

    private LocationVisibility parseVisibility(String s) {
        if (!StringUtils.hasText(s)) return LocationVisibility.HIDDEN;
        try {
            return LocationVisibility.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return LocationVisibility.HIDDEN;
        }
    }

    private Double toDouble(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }
}
