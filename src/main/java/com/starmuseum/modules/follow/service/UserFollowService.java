package com.starmuseum.modules.follow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.follow.vo.FollowUserVO;

public interface UserFollowService {

    void follow(Long currentUserId, Long targetUserId);

    void unfollow(Long currentUserId, Long targetUserId);

    boolean isFollowing(Long currentUserId, Long targetUserId);

    IPage<FollowUserVO> myFollowingPage(Long currentUserId, int page, int size);

    IPage<FollowUserVO> myFollowersPage(Long currentUserId, int page, int size);
}
