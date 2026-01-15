package com.starmuseum.modules.governance.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.AdminGuard;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.governance.admin.dto.AdminReportReviewRequest;
import com.starmuseum.modules.governance.admin.service.AdminReportService;
import com.starmuseum.modules.governance.admin.vo.AdminReportDetailVO;
import com.starmuseum.modules.governance.admin.vo.AdminReportListItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final AdminGuard adminGuard;

    public AdminReportController(AdminReportService adminReportService, AdminGuard adminGuard) {
        this.adminReportService = adminReportService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public Result<IPage<AdminReportListItemVO>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String targetType,
                                                     @RequestParam(required = false) String reasonCode) {
        Long adminUserId = CurrentUser.requireUserId();
        adminGuard.requireAdmin(adminUserId);

        return Result.ok(adminReportService.page(page, size, status, targetType, reasonCode));
    }

    @GetMapping("/{id}")
    public Result<AdminReportDetailVO> detail(@PathVariable("id") Long id) {
        Long adminUserId = CurrentUser.requireUserId();
        adminGuard.requireAdmin(adminUserId);

        return Result.ok(adminReportService.detail(id));
    }

    @PostMapping("/{id}/start")
    public Result<AdminReportDetailVO> start(@PathVariable("id") Long id) {
        Long adminUserId = CurrentUser.requireUserId();
        adminGuard.requireAdmin(adminUserId);

        return Result.ok(adminReportService.start(id, adminUserId));
    }

    @PostMapping("/{id}/review")
    public Result<AdminReportDetailVO> review(@PathVariable("id") Long id,
                                              @RequestBody @Valid AdminReportReviewRequest req) {
        Long adminUserId = CurrentUser.requireUserId();
        adminGuard.requireAdmin(adminUserId);

        return Result.ok(adminReportService.review(id, adminUserId, req));
    }
}
