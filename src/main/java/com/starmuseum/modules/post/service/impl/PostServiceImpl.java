package com.starmuseum.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.entity.UserPrivacySetting;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.iam.mapper.UserPrivacySettingMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PostServiceImpl(PostMapper postMapper,
                           PostMediaMapper postMediaMapper,
                           MediaMapper mediaMapper,
                           UserMapper userMapper,
                           UserPrivacySettingMapper privacySettingMapper,
                           PostLikeMapper postLikeMapper) {
        this.postMapper = postMapper;
        this.postMediaMapper = postMediaMapper;
        this.mediaMapper = mediaMapper;
        this.userMapper = userMapper;
        this.privacySettingMapper = privacySettingMapper;
        this.postLikeMapper = postLikeMapper;
    }

    @Override
    @Transactional
    public PostDetailResponse create(PostCreateRequest req, Long currentUserId) {
        if (req == null) {
            throw new IllegalArgumentException("request is null");
        }

        String visibility = resolveVisibility(req.getVisibility(), currentUserId);

        Post post = new Post();
        post.setUserId(currentUserId);
        post.setContent(req.getContent());
        post.setVisibility(visibility);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
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

    @Override
    public PostDetailResponse detail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null) {
            throw new IllegalArgumentException("Post not found");
        }

        // 可见性：PRIVATE 仅本人可见；FOLLOWERS 预留（当前按 PUBLIC 处理）
        if ("PRIVATE".equalsIgnoreCase(post.getVisibility())
            && (currentUserId == null || !Objects.equals(post.getUserId(), currentUserId))) {
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
        if (currentUserId == null) {
            resp.setLikedByMe(false);
        } else {
            PostLike pl = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, currentUserId)
                .last("LIMIT 1"));
            resp.setLikedByMe(pl != null);
        }

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
        Page<Post> p = new Page<>(page, size);
        IPage<Post> ip = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
            .eq(Post::getVisibility, "PUBLIC")
            .isNull(Post::getDeletedAt)
            .orderByDesc(Post::getCreatedAt));

        Page<PostDetailResponse> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<PostDetailResponse> list = new ArrayList<>();

        for (Post post : ip.getRecords()) {
            list.add(detail(post.getId(), null));
        }
        out.setRecords(list);
        return out;
    }

    @Override
    public IPage<PostDetailResponse> userPage(int page, int size, Long userId) {
        Page<Post> p = new Page<>(page, size);
        IPage<Post> ip = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
            .eq(Post::getUserId, userId)
            .isNull(Post::getDeletedAt)
            .orderByDesc(Post::getCreatedAt));

        Page<PostDetailResponse> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        List<PostDetailResponse> list = new ArrayList<>();

        for (Post post : ip.getRecords()) {
            // 他人主页：只展示 PUBLIC（PRIVATE 不展示）
            if (!"PUBLIC".equalsIgnoreCase(post.getVisibility())) {
                continue;
            }
            list.add(detail(post.getId(), null));
        }
        out.setRecords(list);
        return out;
    }
}
