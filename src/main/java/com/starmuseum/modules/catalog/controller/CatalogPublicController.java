package com.starmuseum.modules.catalog.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.modules.catalog.service.CatalogVersionAdminService;
import com.starmuseum.modules.catalog.vo.CatalogVersionVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sky/catalog")
public class CatalogPublicController {

    private final CatalogVersionAdminService versionAdminService;

    public CatalogPublicController(CatalogVersionAdminService versionAdminService) {
        this.versionAdminService = versionAdminService;
    }

    @GetMapping("/active")
    public Result<CatalogVersionVO> active() {
        return Result.ok(versionAdminService.getActive());
    }
}
