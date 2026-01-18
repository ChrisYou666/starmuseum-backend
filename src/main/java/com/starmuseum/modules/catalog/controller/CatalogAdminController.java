package com.starmuseum.modules.catalog.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.common.security.AdminGuard;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.catalog.dto.CatalogActivateRequest;
import com.starmuseum.modules.catalog.dto.CatalogImportResponse;
import com.starmuseum.modules.catalog.dto.CatalogRollbackRequest;
import com.starmuseum.modules.catalog.dto.CatalogValidateResponse;
import com.starmuseum.modules.catalog.service.CatalogImportService;
import com.starmuseum.modules.catalog.service.CatalogValidationService;
import com.starmuseum.modules.catalog.service.CatalogVersionAdminService;
import com.starmuseum.modules.catalog.vo.CatalogVersionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/catalog")
public class CatalogAdminController {

    private final AdminGuard adminGuard;
    private final CatalogImportService importService;
    private final CatalogValidationService validationService;
    private final CatalogVersionAdminService versionAdminService;

    public CatalogAdminController(AdminGuard adminGuard,
                                  CatalogImportService importService,
                                  CatalogValidationService validationService,
                                  CatalogVersionAdminService versionAdminService) {
        this.adminGuard = adminGuard;
        this.importService = importService;
        this.validationService = validationService;
        this.versionAdminService = versionAdminService;
    }

    @PostMapping("/import")
    public Result<CatalogImportResponse> importZip(@RequestParam("file") MultipartFile file) {
        Long uid = CurrentUser.requireUserId();
        adminGuard.requireAdmin(uid);
        return Result.ok(importService.importZip(file));
    }

    @PostMapping("/validate/{code}")
    public Result<CatalogValidateResponse> validate(@PathVariable("code") String code) {
        Long uid = CurrentUser.requireUserId();
        adminGuard.requireAdmin(uid);
        return Result.ok(validationService.validate(code));
    }

    @PostMapping("/activate")
    public Result<CatalogVersionVO> activate(@RequestBody @Valid CatalogActivateRequest req) {
        Long uid = CurrentUser.requireUserId();
        adminGuard.requireAdmin(uid);
        return Result.ok(versionAdminService.activate(req.getCode()));
    }

    @PostMapping("/rollback")
    public Result<CatalogVersionVO> rollback(@RequestBody(required = false) CatalogRollbackRequest req) {
        Long uid = CurrentUser.requireUserId();
        adminGuard.requireAdmin(uid);
        String target = req == null ? null : req.getTargetCode();
        return Result.ok(versionAdminService.rollback(target));
    }

    @GetMapping("/active")
    public Result<CatalogVersionVO> active() {
        Long uid = CurrentUser.requireUserId();
        adminGuard.requireAdmin(uid);
        return Result.ok(versionAdminService.getActive());
    }

    @GetMapping("/list")
    public Result<List<CatalogVersionVO>> list() {
        Long uid = CurrentUser.requireUserId();
        adminGuard.requireAdmin(uid);
        return Result.ok(versionAdminService.listAll());
    }
}
