package com.starmuseum.modules.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.post.dto.PostCreateRequest;
import com.starmuseum.modules.post.vo.PostDetailResponse;

public interface PostService {

    PostDetailResponse create(PostCreateRequest req, Long currentUserId);

    PostDetailResponse detail(Long postId, Long currentUserId);

    IPage<PostDetailResponse> myPage(int page, int size, Long currentUserId);

    void deleteMy(Long postId, Long currentUserId);

    IPage<PostDetailResponse> publicPage(int page, int size);

    IPage<PostDetailResponse> userPage(int page, int size, Long userId);
}
