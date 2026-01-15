package com.starmuseum.modules.governance.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.governance.report.dto.ReportCreateRequest;
import com.starmuseum.modules.governance.report.service.ReportService;
import com.starmuseum.modules.governance.report.vo.ReportVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 创建举报
     * POST /api/reports
     */
    @PostMapping
    public Result<ReportVO> create(@RequestBody @Valid ReportCreateRequest req) {
        Long reporterId = CurrentUser.requireUserId();
        return Result.ok(reportService.create(req, reporterId));
    }

    /**
     * 我的举报列表
     * GET /api/reports/my?page=&size=&status=
     */
    @GetMapping("/my")
    public Result<IPage<ReportVO>> myPage(
        @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
        @RequestParam(value = "size", defaultValue = "10") @Min(1) int size,
        @RequestParam(value = "status", required = false) String status
    ) {
        Long reporterId = CurrentUser.requireUserId();
        return Result.ok(reportService.myPage(page, size, reporterId, status));
    }

    /**
     * 我的举报详情
     * GET /api/reports/my/{id}
     */
    @GetMapping("/my/{id}")
    public Result<ReportVO> myDetail(@PathVariable("id") Long id) {
        Long reporterId = CurrentUser.requireUserId();
        return Result.ok(reportService.myDetail(id, reporterId));
    }

    /**
     * 撤回举报（仅 OPEN → WITHDRAWN）
     * DELETE /api/reports/my/{id}
     */
    @DeleteMapping("/my/{id}")
    public Result<Void> withdraw(@PathVariable("id") Long id) {
        Long reporterId = CurrentUser.requireUserId();
        reportService.withdraw(id, reporterId);
        return Result.ok();
    }
}
