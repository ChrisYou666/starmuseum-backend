package com.starmuseum.modules.astro.service;

import com.starmuseum.modules.astro.dto.AstroRecommendRequest;
import com.starmuseum.modules.astro.vo.AstroRecommendItemVO;

import java.util.List;

public interface AstroRecommendService {

    List<AstroRecommendItemVO> recommend(AstroRecommendRequest req, Long currentUserIdOrNull);
}
