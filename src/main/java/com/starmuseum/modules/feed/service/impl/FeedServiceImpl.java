package com.starmuseum.modules.feed.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.block.service.UserBlockService;
import com.starmuseum.modules.feed.enums.FeedMode;
import com.starmuseum.modules.feed.mapper.FeedMapper;
import com.starmuseum.modules.feed.service.FeedService;
import com.starmuseum.modules.post.service.PostService;
import com.starmuseum.modules.post.vo.PostDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedMapper feedMapper;
    private final PostService postService;

    // ✅ 用你的真实接口：modules.block.service.UserBlockService
    private final UserBlockService userBlockService;

    @Override
    public IPage<PostDetailResponse> recommend(int page, int size, FeedMode mode, Long viewerUserId) {
        int p = Math.max(1, page);
        int s = Math.max(1, Math.min(size, 50));
        FeedMode m = (mode == null) ? FeedMode.MIX : mode;

        // 热门允许匿名；关注/MIX 需要登录
        if ((m == FeedMode.FOLLOW || m == FeedMode.MIX) && viewerUserId == null) {
            throw new BizException(401, "未登录");
        }

        // ✅ 你的接口返回 Set<Long>
        Set<Long> invisibleSet = (viewerUserId == null)
            ? Collections.emptySet()
            : userBlockService.getInvisibleUserIds(viewerUserId);

        // Mapper 里是 csv 方式 + size 判断
        List<Long> invisibleUserIds = (invisibleSet == null || invisibleSet.isEmpty())
            ? Collections.emptyList()
            : new ArrayList<>(invisibleSet);

        String invisibleCsv = toSafeIdCsv(invisibleUserIds);
        int invisibleSize = invisibleUserIds.size();

        if (m == FeedMode.FOLLOW) {
            return followFeed(p, s, viewerUserId, invisibleSize, invisibleCsv);
        }
        if (m == FeedMode.HOT) {
            return hotFeed(p, s, viewerUserId, invisibleSize, invisibleCsv);
        }

        // MIX：首屏关注优先，不足用热门补
        // MVP 简化策略：只对 page=1 做 MIX，page>1 直接走 HOT
        if (p > 1) {
            return hotFeed(p, s, viewerUserId, invisibleSize, invisibleCsv);
        }

        // page=1: follow + hot fill
        List<Long> followIds = feedMapper.selectFollowFeedPostIds(viewerUserId, invisibleSize, invisibleCsv, s, 0);
        LinkedHashSet<Long> merged = new LinkedHashSet<>(followIds);

        if (merged.size() < s) {
            int remain = s - merged.size();
            // 多取一点避免去重后不够
            List<Long> hotIds = feedMapper.selectHotFeedPostIds(invisibleSize, invisibleCsv, remain * 2, 0);
            for (Long id : hotIds) {
                if (merged.size() >= s) break;
                merged.add(id);
            }
        }

        long totalFollow = feedMapper.countFollowFeed(viewerUserId, invisibleSize, invisibleCsv);
        long totalHot = feedMapper.countHotFeed(invisibleSize, invisibleCsv);
        long total = totalFollow + totalHot; // MIX 下粗略 total 足够用于分页 UI

        List<PostDetailResponse> records = merged.stream()
            .map(id -> postService.detail(id, viewerUserId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Page<PostDetailResponse> resp = new Page<>(p, s);
        resp.setTotal(total);
        resp.setRecords(records);
        return resp;
    }

    private IPage<PostDetailResponse> followFeed(int page, int size, Long viewerUserId, int invisibleSize, String invisibleCsv) {
        long total = feedMapper.countFollowFeed(viewerUserId, invisibleSize, invisibleCsv);
        long offset = (long) (page - 1) * size;

        List<Long> ids = feedMapper.selectFollowFeedPostIds(viewerUserId, invisibleSize, invisibleCsv, size, offset);

        List<PostDetailResponse> records = ids.stream()
            .map(id -> postService.detail(id, viewerUserId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Page<PostDetailResponse> resp = new Page<>(page, size);
        resp.setTotal(total);
        resp.setRecords(records);
        return resp;
    }

    private IPage<PostDetailResponse> hotFeed(int page, int size, Long viewerUserId, int invisibleSize, String invisibleCsv) {
        long total = feedMapper.countHotFeed(invisibleSize, invisibleCsv);
        long offset = (long) (page - 1) * size;

        List<Long> ids = feedMapper.selectHotFeedPostIds(invisibleSize, invisibleCsv, size, offset);

        List<PostDetailResponse> records = ids.stream()
            .map(id -> postService.detail(id, viewerUserId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Page<PostDetailResponse> resp = new Page<>(page, size);
        resp.setTotal(total);
        resp.setRecords(records);
        return resp;
    }

    /**
     * 把 Long 列表转成 “1,2,3” 形式，且只允许数字/逗号（防 SQL 注入）
     */
    private String toSafeIdCsv(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return "0";
        String csv = ids.stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        if (!csv.matches("[0-9,]+")) {
            return "0";
        }
        return csv;
    }
}
