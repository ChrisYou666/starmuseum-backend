package com.starmuseum.modules.follow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.modules.follow.entity.UserFollow;
import com.starmuseum.modules.follow.mapper.UserFollowMapper;
import com.starmuseum.modules.follow.service.UserFollowService;
import com.starmuseum.modules.follow.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    @Override
    public void follow(Long currentUserId, Long targetUserId) {
        if (currentUserId == null) throw new BizException(401, "未登录");
        if (targetUserId == null) throw new BizException(400, "targetUserId 不能为空");
        if (currentUserId.equals(targetUserId)) throw new BizException(400, "不能关注自己");

        // 目标用户必须存在（可选，但建议）
        User target = userMapper.selectById(targetUserId);
        if (target == null) throw new BizException(404, "用户不存在");

        // 幂等：已关注则直接返回
        LambdaQueryWrapper<UserFollow> w = new LambdaQueryWrapper<>();
        w.eq(UserFollow::getFollowerId, currentUserId);
        w.eq(UserFollow::getFolloweeId, targetUserId);
        Long cnt = userFollowMapper.selectCount(w);
        if (cnt != null && cnt > 0) return;

        UserFollow uf = new UserFollow();
        uf.setFollowerId(currentUserId);
        uf.setFolloweeId(targetUserId);
        userFollowMapper.insert(uf);
    }

    @Override
    public void unfollow(Long currentUserId, Long targetUserId) {
        if (currentUserId == null) throw new BizException(401, "未登录");
        if (targetUserId == null) throw new BizException(400, "targetUserId 不能为空");

        LambdaQueryWrapper<UserFollow> w = new LambdaQueryWrapper<>();
        w.eq(UserFollow::getFollowerId, currentUserId);
        w.eq(UserFollow::getFolloweeId, targetUserId);
        userFollowMapper.delete(w);
    }

    @Override
    public boolean isFollowing(Long currentUserId, Long targetUserId) {
        if (currentUserId == null || targetUserId == null) return false;
        LambdaQueryWrapper<UserFollow> w = new LambdaQueryWrapper<>();
        w.eq(UserFollow::getFollowerId, currentUserId);
        w.eq(UserFollow::getFolloweeId, targetUserId);
        Long cnt = userFollowMapper.selectCount(w);
        return cnt != null && cnt > 0;
    }

    @Override
    public IPage<FollowUserVO> myFollowingPage(Long currentUserId, int page, int size) {
        if (currentUserId == null) throw new BizException(401, "未登录");

        Page<UserFollow> p = new Page<>(page, size);
        LambdaQueryWrapper<UserFollow> w = new LambdaQueryWrapper<>();
        w.eq(UserFollow::getFollowerId, currentUserId);
        w.orderByDesc(UserFollow::getCreatedAt);

        IPage<UserFollow> followPage = userFollowMapper.selectPage(p, w);
        List<UserFollow> records = followPage.getRecords();
        List<Long> ids = records.stream().map(UserFollow::getFolloweeId).distinct().toList();

        Map<Long, User> userMap = ids.isEmpty()
            ? Map.of()
            : userMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<FollowUserVO> out = records.stream().map(r -> {
            User u = userMap.get(r.getFolloweeId());
            FollowUserVO vo = new FollowUserVO();
            vo.setUserId(r.getFolloweeId());
            vo.setFollowedAt(r.getCreatedAt());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatarUrl(u.getAvatarUrl());
            }
            return vo;
        }).toList();

        Page<FollowUserVO> resp = new Page<>(page, size);
        resp.setTotal(followPage.getTotal());
        resp.setRecords(out);
        return resp;
    }

    @Override
    public IPage<FollowUserVO> myFollowersPage(Long currentUserId, int page, int size) {
        if (currentUserId == null) throw new BizException(401, "未登录");

        Page<UserFollow> p = new Page<>(page, size);
        LambdaQueryWrapper<UserFollow> w = new LambdaQueryWrapper<>();
        w.eq(UserFollow::getFolloweeId, currentUserId);
        w.orderByDesc(UserFollow::getCreatedAt);

        IPage<UserFollow> followerPage = userFollowMapper.selectPage(p, w);
        List<UserFollow> records = followerPage.getRecords();
        List<Long> ids = records.stream().map(UserFollow::getFollowerId).distinct().toList();

        Map<Long, User> userMap = ids.isEmpty()
            ? Map.of()
            : userMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<FollowUserVO> out = records.stream().map(r -> {
            User u = userMap.get(r.getFollowerId());
            FollowUserVO vo = new FollowUserVO();
            vo.setUserId(r.getFollowerId());
            vo.setFollowedAt(r.getCreatedAt());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatarUrl(u.getAvatarUrl());
            }
            return vo;
        }).toList();

        Page<FollowUserVO> resp = new Page<>(page, size);
        resp.setTotal(followerPage.getTotal());
        resp.setRecords(out);
        return resp;
    }
}
