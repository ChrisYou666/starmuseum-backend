package com.starmuseum.modules.privacy.service;

import com.starmuseum.common.vo.LocationVO;
import com.starmuseum.modules.post.entity.Post;

/**
 * 位置隐私服务（阶段3.1：Post 维度）
 */
public interface LocationPrivacyService {

    /**
     * 根据帖子本身的 location 字段 + viewerId（观看者）构建最终返回 LocationVO
     *
     * 规则（阶段3固定策略）：
     * - viewerId == post.userId：EXACT 下发精确坐标
     * - 否则：
     *   - HIDDEN：null
     *   - CITY：仅 cityName
     *   - FUZZY：下发 fuzzy 坐标
     *   - EXACT：降级为 FUZZY（阶段3固定策略）
     */
    LocationVO buildLocationForViewer(Post post, Long viewerId);
}
