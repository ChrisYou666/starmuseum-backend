package com.starmuseum.modules.feed.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.feed.enums.FeedMode;
import com.starmuseum.modules.post.vo.PostDetailResponse;

public interface FeedService {

    IPage<PostDetailResponse> recommend(int page, int size, FeedMode mode, Long viewerUserId);
}
