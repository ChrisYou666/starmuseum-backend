package com.starmuseum.modules.stats.service.impl;

import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.stats.dto.StatsRangeRequest;
import com.starmuseum.modules.stats.mapper.StatsMapper;
import com.starmuseum.modules.stats.service.StatsService;
import com.starmuseum.modules.stats.vo.MethodCountVO;
import com.starmuseum.modules.stats.vo.MyStatsResponse;
import com.starmuseum.modules.stats.vo.TargetHotItemVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;

    public StatsServiceImpl(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    @Override
    public MyStatsResponse myStats(Long userId, StatsRangeRequest range) {
        if (userId == null) throw new BizException(401, "未登录");
        LocalDateTime from = range.getFrom();
        LocalDateTime to = range.getTo();
        validateRange(from, to);

        // 1) 核心指标
        Long total = statsMapper.countMyLogs(userId, from, to);
        Long published = statsMapper.countMyPublishedLogs(userId, from, to);
        Long successCount = statsMapper.countMySuccessLogs(userId, from, to);
        Double avgRating = statsMapper.avgMyRating(userId, from, to);

        // 2) 分布
        List<MethodCountVO> methodDist = statsMapper.myMethodDistribution(userId, from, to);

        // 3) Top 目标
        int top = normalizeTop(range.getTop(), 10);
        List<TargetHotItemVO> topTargets = statsMapper.myTopTargets(userId, from, to, top);

        // 4) 组装响应
        MyStatsResponse resp = new MyStatsResponse();
        resp.setFrom(from);
        resp.setTo(to);

        resp.setLogCount(nvl(total));
        resp.setPublishedCount(nvl(published));
        resp.setSuccessCount(nvl(successCount));
        resp.setSuccessRate(calcRate(successCount, total));
        resp.setAvgRating(avgRating == null ? null : round2(avgRating));

        resp.setMethodDistribution(methodDist);
        resp.setTopTargets(topTargets);

        return resp;
    }

    @Override
    public MyStatsResponse myMonth(Long userId, String month, int top) {
        if (userId == null) throw new BizException(401, "未登录");

        YearMonth ym;
        if (month == null || month.trim().isEmpty()) {
            ym = YearMonth.now();
        } else {
            try {
                ym = YearMonth.parse(month.trim());
            } catch (Exception e) {
                throw new BizException(400, "month 格式必须为 YYYY-MM");
            }
        }

        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        StatsRangeRequest req = new StatsRangeRequest();
        req.setFrom(from);
        req.setTo(to);
        req.setTop(normalizeTop(top, 10));

        return myStats(userId, req);
    }

    @Override
    public List<TargetHotItemVO> hotTargets(StatsRangeRequest range, int top) {
        LocalDateTime from = range.getFrom();
        LocalDateTime to = range.getTo();
        validateRange(from, to);

        int lim = normalizeTop(top, 10);
        return statsMapper.hotTargets(from, to, lim);
    }

    // ===== helpers =====

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) throw new BizException(400, "from/to 不能为空");
        if (!to.isAfter(from)) throw new BizException(400, "to 必须大于 from");
        // 防止一次查太大范围（MVP 防护，可按需调整）
        if (from.isBefore(LocalDate.now().minusYears(3).atStartOfDay())) {
            throw new BizException(400, "from 范围过大（最多支持查询近 3 年）");
        }
    }

    private int normalizeTop(Integer top, int def) {
        int t = top == null ? def : top;
        if (t <= 0) t = def;
        if (t > 50) t = 50;
        return t;
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private Double calcRate(Long success, Long total) {
        long t = nvl(total);
        if (t <= 0) return null;
        long s = nvl(success);
        return round2((double) s * 100.0 / (double) t); // 返回百分比
    }

    private Double round2(Double v) {
        if (v == null) return null;
        return Math.round(v * 100.0) / 100.0;
    }
}
