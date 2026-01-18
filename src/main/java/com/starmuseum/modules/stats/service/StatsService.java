package com.starmuseum.modules.stats.service;

import com.starmuseum.modules.stats.dto.StatsRangeRequest;
import com.starmuseum.modules.stats.vo.MyStatsResponse;
import com.starmuseum.modules.stats.vo.TargetHotItemVO;

import java.util.List;

public interface StatsService {

    MyStatsResponse myStats(Long userId, StatsRangeRequest range);

    MyStatsResponse myMonth(Long userId, String month, int top);

    List<TargetHotItemVO> hotTargets(StatsRangeRequest range, int top);
}
