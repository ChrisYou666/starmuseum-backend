package com.starmuseum.starmuseum.location.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.starmuseum.common.PageResponse;
import com.starmuseum.starmuseum.common.Result;
import com.starmuseum.starmuseum.location.dto.LocationCreateRequest;
import com.starmuseum.starmuseum.location.dto.LocationUpdateRequest;
import com.starmuseum.starmuseum.location.entity.Location;
import com.starmuseum.starmuseum.location.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 地点管理
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Validated
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody LocationCreateRequest req) {
        return Result.ok(locationService.create(req));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody LocationUpdateRequest req) {
        locationService.update(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        locationService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Location> detail(@PathVariable("id") Long id) {
        return Result.ok(locationService.detail(id));
    }

    /**
     * 分页查询
     * 示例：GET /api/location?pageNum=1&pageSize=10&keyword=北京
     */
    @GetMapping
    public Result<PageResponse<Location>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 必须 >= 1") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 必须 >= 1") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        IPage<Location> page = locationService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResponse.from(page));
    }
}