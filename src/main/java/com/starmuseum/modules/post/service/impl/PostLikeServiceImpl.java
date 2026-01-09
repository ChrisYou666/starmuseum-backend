package com.starmuseum.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.modules.post.entity.Post;
import com.starmuseum.modules.post.entity.PostLike;
import com.starmuseum.modules.post.mapper.PostLikeMapper;
import com.starmuseum.modules.post.mapper.PostMapper;
import com.starmuseum.modules.post.service.PostLikeService;
import com.starmuseum.modules.post.vo.PostLikeResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PostLikeServiceImpl implements PostLikeService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;

    public PostLikeServiceImpl(PostMapper postMapper, PostLikeMapper postLikeMapper) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        if (postId == null || userId == null) return false;
        PostLike pl = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getUserId, userId)
            .last("LIMIT 1"));
        return pl != null;
    }

    @Override
    @Transactional
    public PostLikeResponse like(Long postId, Long userId) {
        if (postId == null) throw new IllegalArgumentException("postId is null");
        if (userId == null) throw new IllegalArgumentException("userId is null");

        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null) {
            throw new IllegalArgumentException("Post not found");
        }

        // PRIVATE 仅作者可操作（你如果允许“看不到也能点赞”可以删掉这段）
        if ("PRIVATE".equalsIgnoreCase(post.getVisibility())
            && !Objects.equals(post.getUserId(), userId)) {
            throw new IllegalArgumentException("无权点赞该帖子");
        }

        // 已点赞就直接返回（不重复+1）
        PostLike exists = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getUserId, userId)
            .last("LIMIT 1"));
        if (exists != null) {
            Post latest = postMapper.selectById(postId);
            PostLikeResponse resp = new PostLikeResponse();
            resp.setPostId(postId);
            resp.setLiked(true);
            resp.setLikeCount(latest == null ? 0 : latest.getLikeCount());
            return resp;
        }

        // 插入点赞记录（依赖 uk_post_user 防并发重复）
        try {
            PostLike pl = new PostLike();
            pl.setPostId(postId);
            pl.setUserId(userId);
            pl.setCreatedAt(LocalDateTime.now());
            postLikeMapper.insert(pl);
        } catch (DuplicateKeyException e) {
            // 并发情况下：另一个请求已插入成功，这里当作已点赞处理
        }

        postMapper.incLikeCount(postId);

        Post latest = postMapper.selectById(postId);
        PostLikeResponse resp = new PostLikeResponse();
        resp.setPostId(postId);
        resp.setLiked(true);
        resp.setLikeCount(latest == null ? 0 : latest.getLikeCount());
        return resp;
    }

    @Override
    @Transactional
    public PostLikeResponse unlike(Long postId, Long userId) {
        if (postId == null) throw new IllegalArgumentException("postId is null");
        if (userId == null) throw new IllegalArgumentException("userId is null");

        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeletedAt() != null) {
            throw new IllegalArgumentException("Post not found");
        }

        PostLike exists = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getUserId, userId)
            .last("LIMIT 1"));

        // 没点过赞：直接返回当前计数
        if (exists == null) {
            Post latest = postMapper.selectById(postId);
            PostLikeResponse resp = new PostLikeResponse();
            resp.setPostId(postId);
            resp.setLiked(false);
            resp.setLikeCount(latest == null ? 0 : latest.getLikeCount());
            return resp;
        }

        postLikeMapper.deleteById(exists.getId());
        postMapper.decLikeCount(postId);

        Post latest = postMapper.selectById(postId);
        PostLikeResponse resp = new PostLikeResponse();
        resp.setPostId(postId);
        resp.setLiked(false);
        resp.setLikeCount(latest == null ? 0 : latest.getLikeCount());
        return resp;
    }
}
