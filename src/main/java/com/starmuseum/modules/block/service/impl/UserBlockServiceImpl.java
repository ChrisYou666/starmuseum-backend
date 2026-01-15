package com.starmuseum.modules.block.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.iam.entity.User;
import com.starmuseum.iam.mapper.UserMapper;
import com.starmuseum.modules.block.entity.UserBlock;
import com.starmuseum.modules.block.mapper.UserBlockMapper;
import com.starmuseum.modules.block.service.UserBlockService;
import com.starmuseum.modules.block.vo.BlockActionResponse;
import com.starmuseum.modules.block.vo.BlockedUserVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserBlockServiceImpl extends ServiceImpl<UserBlockMapper, UserBlock> implements UserBlockService {

    private final UserMapper userMapper;

    public UserBlockServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public BlockActionResponse block(Long userId, Long blockedUserId) {
        if (userId == null) throw new IllegalArgumentException("userId is null");
        if (blockedUserId == null) throw new IllegalArgumentException("blockedUserId is null");
        if (Objects.equals(userId, blockedUserId)) {
            throw new IllegalArgumentException("不能拉黑自己");
        }

        LocalDateTime now = LocalDateTime.now();

        UserBlock ub = new UserBlock();
        ub.setUserId(userId);
        ub.setBlockedUserId(blockedUserId);
        ub.setCreatedAt(now);

        try {
            this.save(ub);
        } catch (DuplicateKeyException e) {
            // 幂等：已存在则直接返回已存在记录
            UserBlock exist = this.getOne(new LambdaQueryWrapper<UserBlock>()
                .eq(UserBlock::getUserId, userId)
                .eq(UserBlock::getBlockedUserId, blockedUserId)
                .last("LIMIT 1"));
            if (exist != null) {
                BlockActionResponse resp = new BlockActionResponse();
                resp.setBlockedUserId(blockedUserId);
                resp.setCreatedAt(exist.getCreatedAt());
                return resp;
            }
            throw e;
        }

        BlockActionResponse resp = new BlockActionResponse();
        resp.setBlockedUserId(blockedUserId);
        resp.setCreatedAt(now);
        return resp;
    }

    @Override
    public void unblock(Long userId, Long blockedUserId) {
        if (userId == null) throw new IllegalArgumentException("userId is null");
        if (blockedUserId == null) throw new IllegalArgumentException("blockedUserId is null");

        this.remove(new LambdaQueryWrapper<UserBlock>()
            .eq(UserBlock::getUserId, userId)
            .eq(UserBlock::getBlockedUserId, blockedUserId));
    }

    @Override
    public IPage<BlockedUserVO> pageMy(long page, long size, Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is null");

        Page<UserBlock> p = new Page<>(page, size);
        IPage<UserBlock> ip = this.page(p, new LambdaQueryWrapper<UserBlock>()
            .eq(UserBlock::getUserId, userId)
            .orderByDesc(UserBlock::getId));

        List<UserBlock> records = ip.getRecords();
        if (records == null || records.isEmpty()) {
            Page<BlockedUserVO> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
            out.setRecords(Collections.emptyList());
            return out;
        }

        Set<Long> blockedIds = records.stream().map(UserBlock::getBlockedUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(blockedIds)
            .stream()
            .collect(Collectors.toMap(User::getId, x -> x, (a, b) -> a));

        List<BlockedUserVO> voList = new ArrayList<>();
        for (UserBlock r : records) {
            User u = userMap.get(r.getBlockedUserId());
            BlockedUserVO vo = new BlockedUserVO();
            vo.setBlockedUserId(r.getBlockedUserId());
            vo.setCreatedAt(r.getCreatedAt());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatarUrl(u.getAvatarUrl());
            }
            voList.add(vo);
        }

        Page<BlockedUserVO> out = new Page<>(ip.getCurrent(), ip.getSize(), ip.getTotal());
        out.setRecords(voList);
        return out;
    }

    @Override
    public Set<Long> getInvisibleUserIds(Long viewerId) {
        if (viewerId == null) return Collections.emptySet();

        // 1) 我拉黑的人
        List<UserBlock> myBlocks = this.list(new LambdaQueryWrapper<UserBlock>()
            .eq(UserBlock::getUserId, viewerId));

        // 2) 拉黑我的人
        List<UserBlock> blockedMe = this.list(new LambdaQueryWrapper<UserBlock>()
            .eq(UserBlock::getBlockedUserId, viewerId));

        Set<Long> set = new HashSet<>();

        if (myBlocks != null) {
            for (UserBlock b : myBlocks) {
                if (b.getBlockedUserId() != null) {
                    set.add(b.getBlockedUserId());
                }
            }
        }

        if (blockedMe != null) {
            for (UserBlock b : blockedMe) {
                if (b.getUserId() != null) {
                    set.add(b.getUserId());
                }
            }
        }

        return set;
    }
}
