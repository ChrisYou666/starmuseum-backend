package com.starmuseum.starmuseum.celestial.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.starmuseum.celestial.dto.CelestialBodyCreateRequest;
import com.starmuseum.starmuseum.celestial.dto.CelestialBodyQueryRequest;
import com.starmuseum.starmuseum.celestial.dto.CelestialBodyUpdateRequest;
import com.starmuseum.starmuseum.celestial.entity.CelestialBody;
import com.starmuseum.starmuseum.celestial.service.CelestialBodyService;
import com.starmuseum.starmuseum.common.PageResponse;
import com.starmuseum.starmuseum.common.Result;
import com.starmuseum.starmuseum.common.ResultCode;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/celestial-bodies")
@Validated
public class CelestialBodyController {

    private final CelestialBodyService celestialBodyService;

    public CelestialBodyController(CelestialBodyService celestialBodyService) {
        this.celestialBodyService = celestialBodyService;
    }

    @PostMapping
    public Result<CelestialBody> create(@RequestBody @Valid CelestialBodyCreateRequest req) {
        CelestialBody body = new CelestialBody();
        body.setName(req.getName());
        body.setAlias(req.getAlias());
        body.setType(req.getType());
        body.setConstellation(req.getConstellation());
        body.setRaHours(req.getRaHours());
        body.setDecDegrees(req.getDecDegrees());
        body.setMagnitude(req.getMagnitude());
        body.setDistanceLy(req.getDistanceLy());
        body.setSpectralType(req.getSpectralType());
        body.setTemperatureK(req.getTemperatureK());
        body.setDescription(req.getDescription());

        celestialBodyService.save(body);
        return Result.ok(body);
    }

    @PutMapping("/{id}")
    public Result<CelestialBody> update(@PathVariable Long id, @RequestBody @Valid CelestialBodyUpdateRequest req) {
        CelestialBody body = celestialBodyService.getById(id);
        if (body == null) {
            return Result.fail(ResultCode.NOT_FOUND); // ✅ 修复
        }

        body.setName(req.getName());
        body.setAlias(req.getAlias());
        body.setType(req.getType());
        body.setConstellation(req.getConstellation());
        body.setRaHours(req.getRaHours());
        body.setDecDegrees(req.getDecDegrees());
        body.setMagnitude(req.getMagnitude());
        body.setDistanceLy(req.getDistanceLy());
        body.setSpectralType(req.getSpectralType());
        body.setTemperatureK(req.getTemperatureK());
        body.setDescription(req.getDescription());

        celestialBodyService.updateById(body);
        return Result.ok(body);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = celestialBodyService.removeById(id);
        return ok ? Result.ok(null) : Result.fail(ResultCode.NOT_FOUND); // ✅ 修复
    }

    @GetMapping("/{id}")
    public Result<CelestialBody> detail(@PathVariable Long id) {
        CelestialBody body = celestialBodyService.getById(id);
        if (body == null) {
            return Result.fail(ResultCode.NOT_FOUND); // ✅ 修复
        }
        return Result.ok(body);
    }

    @GetMapping("/page")
    public Result<PageResponse<CelestialBody>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            CelestialBodyQueryRequest query
    ) {
        IPage<CelestialBody> page = celestialBodyService.pageQuery(pageNum, pageSize, query);
        return Result.ok(PageResponse.from(page));
    }
}