package com.starmuseum.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.common.enums.LocationVisibility;
import com.starmuseum.common.util.GeoFuzzUtil;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.entity.UserPrivacySetting;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.iam.mapper.UserPrivacySettingMapper;
import com.starmuseum.modules.block.service.UserBlockService;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.mapper.MediaMapper;
import com.starmuseum.modules.post.dto.PostCreateRequest;
import com.starmuseum.modules.post.entity.Post;
import com.starmuseum.modules.post.entity.PostLike;
import com.starmuseum.modules.post.entity.PostMedia;
import com.starmuseum.modules.post.mapper.PostLikeMapper;
import com.starmuseum.modules.post.mapper.PostMapper;
import com.starmuseum.modules.post.mapper.PostMediaMapper;
import com.starmuseum.modules.post.service.PostService;
import com.starmuseum.modules.post.vo.PostDetailResponse;
import com.starmuseum.modules.post.vo.PostMediaItem;
import com.starmuseum.modules.privacy.service.LocationPrivacyService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;
    private final MediaMapper mediaMapper;
    private final UserMapper userMapper;
    private final UserPrivacySettingMapper privacySettingMapper;
    private final PostLikeMapper postLikeMapper;
    private final LocationPrivacyService locationPrivacyService;

    // === 3.4 Block ===
    private final UserBlockService userBlockService;

    public PostServiceImpl(PostMapper postMapper,
                           PostMediaMapper postMediaMapper,
                           MediaMapper mediaMapper,
                           UserMapper userMapper,
                           UserPrivacySettingMapper privacySettingMapper,
                           PostLikeMapper postLikeMapper,
                           LocationPrivacyService locationPrivacyService,
                           UserBlockService userBlockService) {
        this.postMapper = postMapper;
        this.postMediaMapper = postMediaMapper;
        this.mediaMapper = mediaMapper;
        this.userMapper = userMapper;
        this.privacySettingMapper = privacySettingMapper;
        this.postLikeMapper = postLikeMapper;
        this.locationPrivacyService = locationPrivacyService;
        this.userBlockService = userBlockService;
    }

    public PostDetailResponse create(PostCreateRequest req, Long currentUserId) {
        if (req == null) {
            throw new IllegalArgumentException("request is null");
        }

        String visibility = resolveVisibility(req.getVisibility(), currentUserId);

        // =========================
        // 阶段3.2：位置字段（默认从隐私设置取）
        // =========================

        // 1) 解析 locationVisibility：请求不传则走用户默认
        String locationVisibilityStr = resolveLocationVisibility(req.getLocationVisibility(), currentUserId);
        LocationVisibility locationVisibilityEnum = GeoFuzzUtil.parseVisibility(locationVisibilityStr);

        Double lat = req.getLat();
        Double lon = req.getLon();
        String cityName = StringUtils.hasText(req.getCityName()) ? req.getCityName().trim() : null;

        // 2) 校验：lat/lon 要么都传，要么都不传
        if (lat == null && lon != null) {
            throw new IllegalArgumentException("lat/lon 必须成对出现（lat 缺失）");
        }
        if (lat != null && lon == null) {
            throw new IllegalArgumentException("lat/lon 必须成对出现（lon 缺失）");
        }

        // 3) FUZZY/EXACT：必须有坐标
        if ((locationVisibilityEnum == LocationVisibility.FUZZY || locationVisibilityEnum == LocationVisibility.EXACT) && lat == null) {
            throw new IllegalArgumentException("locationVisibility 为 FUZZY/EXACT 时必须提供 lat/lon");
        }

        // 4) 如果提供坐标：范围校验 + 预计算 fuzzy
        Double latFuzzy = null;
        Double lonFuzzy = null;
        if (lat != null) {
            GeoFuzzUtil.validateLatLon(lat, lon);
            latFuzzy = GeoFuzzUtil.fuzz(lat, 2);
            lonFuzzy = GeoFuzzUtil.fuzz(lon, 2);
        }

        // =========================
        // 写入 post
        // =========================
        Post post = new Post();
        post.setUserId(currentUserId);
        post.setContent(req.getContent());
        post.setVisibility(visibility);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // ✅ 关键：写入所有 location 字段（不是只写 visibility）
        post.setLocationVisibility(locationVisibilityStr);
        post.setLocationCity(cityName);
        post.setLocationLat(GeoFuzzUtil.toDecimal6(lat));
        post.setLocationLon(GeoFuzzUtil.toDecimal6(lon));
        post.setLocationLatFuzzy(GeoFuzzUtil.toDecimal6(latFuzzy));
        post.setLocationLonFuzzy(GeoFuzzUtil.toDecimal6(lonFuzzy));

        postMapper.insert(post);

        // 绑定图片（按请求顺序写入 sort_no）
        List<Long> mediaIds = req.getMediaIds();
        if (mediaIds != null && !mediaIds.isEmpty()) {
            if (mediaIds.size() > 9) {
                throw new IllegalArgumentException("最多只允许 9 张图片");
            }

            List<Media> medias = mediaMapper.selectBatchIds(mediaIds);
            Map<Long, Media> mediaMap = new HashMap<>();
            for (Media m : medias) {
                mediaMap.put(m.getId(), m);
            }

            int sortNo = 1;
            for (Long mediaId : mediaIds) {
                if (mediaId == null) continue;

                Media media = mediaMap.get(mediaId);
                if (media == null) {
                    throw new IllegalArgumentException("media not found: " + mediaId);
                }
                if (!Objects.equals(currentUserId, media.getUserId())) {
                    throw new IllegalArgumentException("无权使用他人的图片资源");
                }
                if (!MediaBizType.POST.name().equals(media.getBizType())) {
                    throw new IllegalArgumentException("media bizType 必须为 POST");
                }

                PostMedia pm = new PostMedia();
                pm.setPostId(post.getId());
                pm.setMediaId(mediaId);
                pm.setSortNo(sortNo++);
                pm.setCreatedAt(LocalDateTime.now());
                postMediaMapper.insert(pm);
            }
        }

        return detail(post.getId(), currentUserId);
    }

    private String resolveLocationVisibility(String reqVisibility, Long userId) {
        if (org.springframework.util.StringUtils.hasText(reqVisibility)) {
            String v = reqVisibility.trim().toUpperCase();
            com.starmuseum.common.enums.LocationVisibility.valueOf(v); // 非法直接抛异常
            return v;
        }

        com.starmuseum.iam.entity.UserPrivacySetting setting = privacySettingMapper.selectById(userId);
        if (setting != null && org.springframework.util.StringUtils.hasText(setting.getDefaultLocationVisibility())) {
            String v = setting.getDefaultLocationVisibility().trim().toUpperCase();
            com.starmuseum.common.enums.LocationVisibility.valueOf(v);
            return v;
        }

        return com.starmuseum.common.enums.LocationVisibility.HIDDEN.name();
    }

    @Override
    public PostDetailResponse detail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null) {
            throw new IllegalArgumentException("Post not found");
        }

        // === 3.4 Block：详情不可见直接隐藏（按 404 语义处理）===
        Long viewerId = currentUserId != null ? currentUserId : currentUserIdOrNull();
        if (viewerId != null) {
            Set<Long> invisible = userBlockService.getInvisibleUserIds(viewerId);
            if (invisible != null && invisible.contains(post.getUserId())) {
                throw new IllegalArgumentException("Post not found");
            }
        }

        // 可见性：PRIVATE 仅本人可见；FOLLOWERS 预留（当前按 PUBLIC 处理）
        if ("PRIVATE".equalsIgnoreCase(post.getVisibility())
            && (viewerId == null || !Objects.equals(post.getUserId(), viewerId))) {
            throw new IllegalArgumentException("无权查看该帖子");
        }

        User author = userMapper.selectById(post.getUserId());

        PostDetailResponse resp = new PostDetailResponse();
        resp.setId(post.getId());
        resp.setUserId(post.getUserId());
        resp.setContent(post.getContent());
        resp.setVisibility(post.getVisibility());
        resp.setLikeCount(post.getLikeCount());
        resp.setCommentCount(post.getCommentCount());
        resp.setCreatedAt(post.getCreatedAt());
        resp.setUpdatedAt(post.getUpdatedAt());

        if (author != null) {
            resp.setNickname(author.getNickname());
            resp.setAvatarUrl(author.getAvatarUrl());
        }

        // likedByMe
        if (viewerId == null) {
            resp.setLikedByMe(false);
        } else {
            PostLike pl = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, viewerId)
                .last("LIMIT 1"));
            resp.setLikedByMe(pl != null);
        }

        // 阶段3.1：位置（按隐私规则过滤）
        resp.setLocation(locationPrivacyService.buildLocationForViewer(post, viewerId));

        // 查询图片列表
        List<PostMedia> relations = postMediaMapper.selectList(new LambdaQueryWrapper<PostMedia>()
            .eq(PostMedia::getPostId, postId)
            .orderByAsc(PostMedia::getSortNo));

        if (relations == null || relations.isEmpty()) {
            resp.setMediaList(new ArrayList<>());
            return resp;
        }

        List<Long> mediaIds = new ArrayList<>();
        for (PostMedia r : relations) {
            if (r.getMediaId() != null) {
                mediaIds.add(r.getMediaId());
            }
        }

        List<Media> medias = mediaMapper.selectBatchIds(mediaIds);
        Map<Long, Media> mediaMap = new HashMap<>();
        for (Media m : medias) {
            mediaMap.put(m.getId(), m);
        }

        List<PostMediaItem> items = new ArrayList<>();
        for (PostMedia r : relations) {
            Media m = mediaMap.get(r.getMediaId());
            if (m == null) continue;

            PostMediaItem item = new PostMediaItem();
            item.setMediaId(m.getId());
            item.setSortNo(r.getSortNo());
            item.setOriginUrl(m.getOriginUrl());
            item.setThumbUrl(m.getThumbUrl());
            item.setMediumUrl(m.getMediumUrl());
            item.setMimeType(m.getMimeType());
            item.setSizeBytes(m.getSizeBytes());
            item.setWidth(m.getWidth());
            item.setHeight(m.getHeight());
            items.add(item);
        }

        resp.setMediaList(items);
        return resp;
    }

    @Override
    public IPage<PostDetailResponse> myPage(int page, int size, Long currentUserId) {
        Page<Post> p = new Page<>(page, size);
        IPage<Post> ip = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
            .eq(Post::getUserId, currentUserId)
            .isNull(Post::getDeletedAt)
            .orderByDesc(Post::getId));

        Page<PostDetailResponse> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<PostDetailResponse> list = new ArrayList<>();

        if (ip.getRecords() != null) {
            for (Post post : ip.getRecords()) {
                list.add(detail(post.getId(), currentUserId));
            }
        }
        out.setRecords(list);
        return out;
    }

    @Override
    @Transactional
    public void deleteMy(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null) return;

        if (!Objects.equals(post.getUserId(), currentUserId)) {
            throw new IllegalArgumentException("无权删除他人的帖子");
        }

        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUserId);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
    }

    private String resolveVisibility(String reqVisibility, Long userId) {
        if (reqVisibility != null && !reqVisibility.trim().isEmpty()) {
            return reqVisibility.trim().toUpperCase();
        }

        UserPrivacySetting setting = privacySettingMapper.selectById(userId);
        if (setting != null && setting.getPostVisibilityDefault() != null
            && !setting.getPostVisibilityDefault().trim().isEmpty()) {
            return setting.getPostVisibilityDefault().trim().toUpperCase();
        }

        return "PUBLIC";
    }

    @Override
    public IPage<PostDetailResponse> publicPage(int page, int size) {
        Long viewerId = currentUserIdOrNull();
        Set<Long> invisible = (viewerId == null) ? Collections.emptySet() : userBlockService.getInvisibleUserIds(viewerId);

        Page<Post> p = new Page<>(page, size);
        IPage<Post> ip = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
            .eq(Post::getVisibility, "PUBLIC")
            .isNull(Post::getDeletedAt)
            // === 3.4 Block：公共流过滤 ===
            .notIn(invisible != null && !invisible.isEmpty(), Post::getUserId, invisible)
            .orderByDesc(Post::getCreatedAt));

        Page<PostDetailResponse> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<PostDetailResponse> list = new ArrayList<>();

        for (Post post : ip.getRecords()) {
            list.add(detail(post.getId(), viewerId));
        }
        out.setRecords(list);
        return out;
    }

    @Override
    public IPage<PostDetailResponse> userPage(int page, int size, Long userId) {
        Long viewerId = currentUserIdOrNull();
        Set<Long> invisible = (viewerId == null) ? Collections.emptySet() : userBlockService.getInvisibleUserIds(viewerId);

        Page<Post> p = new Page<>(page, size);
        IPage<Post> ip = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
            .eq(Post::getUserId, userId)
            .isNull(Post::getDeletedAt)
            // === 3.4 Block：用户公开流过滤（如果该 user 被屏蔽，会直接变空）===
            .notIn(invisible != null && !invisible.isEmpty(), Post::getUserId, invisible)
            .orderByDesc(Post::getCreatedAt));

        Page<PostDetailResponse> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<PostDetailResponse> list = new ArrayList<>();

        for (Post post : ip.getRecords()) {
            // 他人主页：只展示 PUBLIC（PRIVATE 不展示）
            if (!"PUBLIC".equalsIgnoreCase(post.getVisibility())) {
                continue;
            }
            list.add(detail(post.getId(), viewerId));
        }
        out.setRecords(list);
        return out;
    }

    /**
     * 尝试从 SecurityContext 获取当前用户（未登录返回 null）
     */
    private Long currentUserIdOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) return null;

            Object principal = auth.getPrincipal();

            if (principal instanceof Long) return (Long) principal;

            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (Exception ignored) {
                }
            }

            try {
                Method m = principal.getClass().getMethod("getId");
                Object v = m.invoke(principal);
                if (v instanceof Number) return ((Number) v).longValue();
            } catch (Exception ignored) {
            }

            try {
                Method m = principal.getClass().getMethod("getUserId");
                Object v = m.invoke(principal);
                if (v instanceof Number) return ((Number) v).longValue();
            } catch (Exception ignored) {
            }

            try {
                Method m = principal.getClass().getMethod("getUsername");
                Object v = m.invoke(principal);
                if (v != null) return Long.parseLong(v.toString());
            } catch (Exception ignored) {
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
