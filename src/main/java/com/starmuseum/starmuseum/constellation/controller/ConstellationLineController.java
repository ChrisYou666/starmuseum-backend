package com.starmuseum.starmuseum.constellation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.starmuseum.common.PageResponse;
import com.starmuseum.starmuseum.common.Result;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineCreateRequest;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineQueryRequest;
import com.starmuseum.starmuseum.constellation.vo.ConstellationLineSegment;
import com.starmuseum.starmuseum.constellation.dto.ConstellationLineUpdateRequest;
import com.starmuseum.starmuseum.constellation.entity.ConstellationLine;
import com.starmuseum.starmuseum.constellation.service.ConstellationLineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/constellation-lines")
public class ConstellationLineController {

    private final ConstellationLineService constellationLineService;

    public ConstellationLineController(ConstellationLineService constellationLineService) {
        this.constellationLineService = constellationLineService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid ConstellationLineCreateRequest req) {
        Long id = constellationLineService.createLine(req);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable("id") Long id,
                                  @RequestBody @Valid ConstellationLineUpdateRequest req) {
        req.setId(id);
        boolean ok = constellationLineService.updateLine(req);
        return Result.success(ok);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean ok = constellationLineService.removeById(id);
        return Result.success(ok);
    }

    @GetMapping("/{id}")
    public Result<ConstellationLine> get(@PathVariable("id") Long id) {
        ConstellationLine line = constellationLineService.getById(id);
        return Result.success(line);
    }

    @GetMapping("/page")
    public Result<PageResponse<ConstellationLine>> page(ConstellationLineQueryRequest req) {
        IPage<ConstellationLine> page = constellationLineService.page(req);
        return Result.success(PageResponse.of(page));
    }

    /**
     * SkyView 用：返回线段（只包含 startBodyId/endBodyId）
     * 可选：?constellationCode=Ori
     */
    @GetMapping("/segments")
    public Result<List<ConstellationLineSegment>> segments(
            @RequestParam(value = "constellationCode", required = false) String constellationCode
    ) {
        List<ConstellationLineSegment> list = constellationLineService.listSegments(constellationCode);
        return Result.success(list);
    }
}