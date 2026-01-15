package com.starmuseum.modules.block.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.block.vo.BlockActionResponse;
import com.starmuseum.modules.block.vo.BlockedUserVO;

import java.util.Set;

public interface UserBlockService {

    BlockActionResponse block(Long userId, Long blockedUserId);

    void unblock(Long userId, Long blockedUserId);

    IPage<BlockedUserVO> pageMy(long page, long size, Long userId);

    /**
     * 对“当前 viewer”不可见的用户集合：
     * 1) viewer 拉黑的人
     * 2) 拉黑 viewer 的人
     *
     * 用于 feed/详情/评论过滤
     */
    Set<Long> getInvisibleUserIds(Long viewerId);
}
