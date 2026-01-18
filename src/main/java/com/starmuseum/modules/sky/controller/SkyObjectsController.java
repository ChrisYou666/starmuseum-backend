package com.starmuseum.modules.sky.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.modules.sky.service.SkyObjectService;
import com.starmuseum.modules.sky.vo.SkyObjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sky")
public class SkyObjectsController {

    private final SkyObjectService skyObjectService;

    /**
     * 按需加载 objects
     * GET /api/sky/objects?version=active&type=STAR&limit=200
     */
    @GetMapping("/objects")
    public Result<List<SkyObjectVO>> objects(
        @RequestParam(value = "version", required = false) String version,
        @RequestParam(value = "type", required = false) String type,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return Result.ok(skyObjectService.getObjects(version, type, limit));
    }
}
