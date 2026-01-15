package com.starmuseum.modules.governance.action.service;

import com.starmuseum.modules.governance.action.dto.ModerationActionCreateRequest;

public interface ModerationActionService {

    /**
     * 执行处罚动作（写 action 记录 + 更新 user/post/comment）
     */
    void apply(ModerationActionCreateRequest req, Long operatorUserId, Long relatedReportId);
}
