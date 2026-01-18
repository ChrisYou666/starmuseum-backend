package com.starmuseum.modules.observation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.observation.dto.ObservationLogCreateRequest;
import com.starmuseum.modules.observation.dto.ObservationLogUpdateRequest;
import com.starmuseum.modules.observation.dto.ObservationPublishRequest;
import com.starmuseum.modules.observation.service.ObservationLogService;
import com.starmuseum.modules.observation.service.ObservationPublishService;
import com.starmuseum.modules.observation.vo.ObservationLogDetailVO;
import com.starmuseum.modules.observation.vo.ObservationLogVO;
import com.starmuseum.modules.observation.vo.ObservationPublishResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/observation/logs")
public class ObservationLogController {

    private final ObservationLogService observationLogService;
    private final ObservationPublishService observationPublishService;

    public ObservationLogController(ObservationLogService observationLogService,
                                    ObservationPublishService observationPublishService) {
        this.observationLogService = observationLogService;
        this.observationPublishService = observationPublishService;
    }

    /**
     * 创建观测日志
     */
    @PostMapping
    public ObservationLogDetailVO create(@Valid @RequestBody ObservationLogCreateRequest req) {
        Long userId = CurrentUser.requireUserId();
        return observationLogService.createMy(userId, req);
    }

    /**
     * 更新观测日志（本人）
     */
    @PutMapping("/{id}")
    public ObservationLogDetailVO update(@PathVariable("id") Long id,
                                         @Valid @RequestBody ObservationLogUpdateRequest req) {
        Long userId = CurrentUser.requireUserId();
        return observationLogService.updateMy(userId, id, req);
    }

    /**
     * 我的观测日志分页
     */
    @GetMapping
    public IPage<ObservationLogVO> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Long userId = CurrentUser.requireUserId();
        return observationLogService.pageMy(userId, page, size);
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ObservationLogDetailVO detail(@PathVariable("id") Long id) {
        Long userId = CurrentUser.requireUserId();
        return observationLogService.getMy(userId, id);
    }

    /**
     * 软删（幂等）
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        Long userId = CurrentUser.requireUserId();
        observationLogService.deleteMy(userId, id);
    }

    /**
     * 一键发布动态（闭环关键）
     */
    @PostMapping("/{id}/publish")
    public ObservationPublishResponse publish(@PathVariable("id") Long id,
                                              @RequestBody(required = false) ObservationPublishRequest req) {
        Long userId = CurrentUser.requireUserId();
        return observationPublishService.publishMy(userId, id, req);
    }
}
