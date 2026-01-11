package com.starmuseum.modules.astro.controller;

import com.starmuseum.modules.astro.dto.CatalogActivateRequest;
import com.starmuseum.modules.astro.entity.CatalogVersion;
import com.starmuseum.modules.astro.service.CatalogVersionService;
import com.starmuseum.modules.astro.vo.CatalogVersionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/astro/catalog")
@RequiredArgsConstructor
public class AstroCatalogController {

    private final CatalogVersionService catalogVersionService;

    /**
     * 当前生效版本
     */
    @GetMapping("/version/active")
    public CatalogVersionVO active() {
        CatalogVersion active = catalogVersionService.getActive();
        return toVO(active);
    }

    /**
     * 版本列表
     */
    @GetMapping("/version/list")
    public List<CatalogVersionVO> list() {
        return catalogVersionService.listAll().stream().map(this::toVO).toList();
    }

    /**
     * 激活版本
     */
    @PostMapping("/version/activate")
    public CatalogVersionVO activate(@Valid @RequestBody CatalogActivateRequest req) {
        CatalogVersion activated = catalogVersionService.activate(req.getCode());
        return toVO(activated);
    }

    private CatalogVersionVO toVO(CatalogVersion e) {
        if (e == null) return null;
        CatalogVersionVO vo = new CatalogVersionVO();
        vo.setId(e.getId());
        vo.setCode(e.getCode());
        vo.setStatus(e.getStatus());
        vo.setChecksum(e.getChecksum());
        vo.setSourceNote(e.getSourceNote());
        vo.setActivatedAt(e.getActivatedAt());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
