package com.starmuseum.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.modules.block.service.UserBlockService;
import com.starmuseum.modules.post.dto.PostCommentCreateRequest;
import com.starmuseum.modules.post.entity.PostComment;
import com.starmuseum.modules.post.mapper.PostCommentMapper;
import com.starmuseum.modules.post.service.PostCommentService;
import com.starmuseum.modules.post.vo.PostCommentResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentMapper commentMapper;
    private final UserMapper userMapper;

    // === 3.4 Block ===
    private final UserBlockService userBlockService;

    public PostCommentServiceImpl(PostCommentMapper commentMapper,
                                  UserMapper userMapper,
                                  UserBlockService userBlockService) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.userBlockService = userBlockService;
    }

    @Override
    @Transactional
    public PostCommentResponse create(Long postId, Long userId, PostCommentCreateRequest req) {
        String content = req.getContent() == null ? null : req.getContent().trim();
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("content 不能为空");
        }

        PostComment c = new PostComment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setContent(content);
        c.setDeleted(0);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());

        commentMapper.insert(c);

        User u = userMapper.selectById(userId);

        PostCommentResponse resp = new PostCommentResponse();
        resp.setId(c.getId());
        resp.setPostId(postId);
        resp.setUserId(userId);
        resp.setContent(c.getContent());
        resp.setCreatedAt(c.getCreatedAt());
        if (u != null) {
            resp.setNickname(u.getNickname());
            resp.setAvatarUrl(u.getAvatarUrl());
        }
        return resp;
    }

    @Override
    public IPage<PostCommentResponse> page(Long postId, int page, int size) {
        Long viewerId = currentUserIdOrNull();
        Set<Long> invisible = (viewerId == null) ? Collections.emptySet() : userBlockService.getInvisibleUserIds(viewerId);

        Page<PostComment> p = new Page<>(page, size);

        IPage<PostComment> entityPage = commentMapper.selectPage(
            p,
            new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getDeleted, 0)
                // === 3.4 Block：评论列表过滤 ===
                .notIn(invisible != null && !invisible.isEmpty(), PostComment::getUserId, invisible)
                .orderByDesc(PostComment::getCreatedAt)
        );

        List<PostComment> records = entityPage.getRecords();
        if (records == null || records.isEmpty()) {
            Page<PostCommentResponse> empty = new Page<>(page, size);
            empty.setTotal(entityPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        Set<Long> userIds = records.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
            .stream()
            .collect(Collectors.toMap(User::getId, x -> x, (a, b) -> a));

        List<PostCommentResponse> respList = records.stream().map(c -> {
            User u = userMap.get(c.getUserId());
            PostCommentResponse r = new PostCommentResponse();
            r.setId(c.getId());
            r.setPostId(c.getPostId());
            r.setUserId(c.getUserId());
            r.setContent(c.getContent());
            r.setCreatedAt(c.getCreatedAt());
            if (u != null) {
                r.setNickname(u.getNickname());
                r.setAvatarUrl(u.getAvatarUrl());
            }
            return r;
        }).toList();

        Page<PostCommentResponse> out = new Page<>(page, size);
        out.setTotal(entityPage.getTotal());
        out.setRecords(respList);
        return out;
    }

    @Override
    @Transactional
    public void deleteMy(Long commentId, Long userId) {
        PostComment c = commentMapper.selectById(commentId);
        if (c == null || Objects.equals(c.getDeleted(), 1)) {
            throw new IllegalArgumentException("comment not found: " + commentId);
        }
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权删除他人的评论");
        }
        // MyBatis-Plus TableLogic：update deleted=1
        commentMapper.deleteById(commentId);
    }

    @Override
    public long countByPostId(Long postId) {
        return commentMapper.selectCount(
            new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getDeleted, 0)
        );
    }

    @Override
    public List<PostCommentResponse> latest(Long postId, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        Long viewerId = currentUserIdOrNull();
        Set<Long> invisible = (viewerId == null) ? Collections.emptySet() : userBlockService.getInvisibleUserIds(viewerId);

        List<PostComment> list = commentMapper.selectList(
            new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getDeleted, 0)
                // === 3.4 Block：最新评论过滤 ===
                .notIn(invisible != null && !invisible.isEmpty(), PostComment::getUserId, invisible)
                .orderByDesc(PostComment::getCreatedAt)
                .last("LIMIT " + limit)
        );

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = list.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
            .stream()
            .collect(Collectors.toMap(User::getId, x -> x, (a, b) -> a));

        return list.stream().map(c -> {
            User u = userMap.get(c.getUserId());
            PostCommentResponse r = new PostCommentResponse();
            r.setId(c.getId());
            r.setPostId(c.getPostId());
            r.setUserId(c.getUserId());
            r.setContent(c.getContent());
            r.setCreatedAt(c.getCreatedAt());
            if (u != null) {
                r.setNickname(u.getNickname());
                r.setAvatarUrl(u.getAvatarUrl());
            }
            return r;
        }).toList();
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
