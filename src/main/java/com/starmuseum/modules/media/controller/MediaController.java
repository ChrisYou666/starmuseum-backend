package com.starmuseum.modules.media.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.common.api.Result;
import com.starmuseum.modules.media.dto.MediaDTO;
import com.starmuseum.modules.media.dto.MediaUploadResponse;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.service.MediaService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Media 模块
 * A：上传
 * B：查询/分页/删除
 *
 * 本次修复点：
 * 1) 上传接口兼容参数名 bizType / type（很多前端会用 type=AVATAR）
 * 2) 增加头像专用上传入口：POST /api/media/upload/avatar （强制 AVATAR）
 */
@Validated
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // =========================
    // A 模块：上传
    // =========================

    /**
     * 头像上传（推荐用这个接口）
     * POST /api/media/upload/avatar
     * form-data: file=<选择图片>
     *
     * 返回的 media.bizType 必为 AVATAR
     */
    @PostMapping("/upload/avatar")
    public Result<MediaUploadResponse> uploadAvatar(
        @RequestParam("file") @NotNull MultipartFile file
    ) {
        MediaUploadResponse resp = mediaService.uploadOne(file, MediaBizType.AVATAR);
        return Result.ok(resp);
    }

    /**
     * 单文件上传
     * POST /api/media/upload?bizType=POST
     * 兼容：POST /api/media/upload?type=POST
     * form-data: file=<选择图片>
     */
    @PostMapping("/upload")
    public Result<MediaUploadResponse> uploadOne(
        @RequestParam("file") @NotNull MultipartFile file,
        @RequestParam(value = "bizType", required = false) MediaBizType bizType,
        @RequestParam(value = "type", required = false) MediaBizType type
    ) {
        MediaBizType effective = resolveBizType(bizType, type, MediaBizType.POST);
        MediaUploadResponse resp = mediaService.uploadOne(file, effective);
        return Result.ok(resp);
    }

    /**
     * 多文件上传（最多9张）
     * POST /api/media/upload/batch?bizType=POST
     * 兼容：POST /api/media/upload/batch?type=POST
     * form-data: files=<多选图片>
     */
    @PostMapping("/upload/batch")
    public Result<List<MediaUploadResponse>> uploadBatch(
        @RequestParam("files") @NotNull List<MultipartFile> files,
        @RequestParam(value = "bizType", required = false) MediaBizType bizType,
        @RequestParam(value = "type", required = false) MediaBizType type
    ) {
        MediaBizType effective = resolveBizType(bizType, type, MediaBizType.POST);
        List<MediaUploadResponse> list = mediaService.uploadBatch(files, effective);
        return Result.ok(list);
    }

    // =========================
    // B 模块：查询/分页/删除
    // =========================

    /**
     * 按 id 查询（默认只能查自己的）
     * GET /api/media/{id}
     */
    @GetMapping("/{id}")
    public Result<MediaDTO> getById(@PathVariable("id") Long id) {
        return Result.ok(mediaService.getDtoById(id));
    }

    /**
     * 分页查询（默认只查当前登录用户自己的 media）
     * GET /api/media/page?page=1&size=10&bizType=POST
     */
    @GetMapping("/page")
    public Result<IPage<MediaDTO>> page(
        @RequestParam(value = "page", defaultValue = "1") @Min(1) long page,
        @RequestParam(value = "size", defaultValue = "10") @Min(1) long size,
        @RequestParam(value = "bizType", required = false) MediaBizType bizType
    ) {
        return Result.ok(mediaService.pageMy(page, size, bizType));
    }

    /**
     * 删除（默认只能删除自己的）
     * DELETE /api/media/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        mediaService.deleteMy(id);
        return Result.ok();
    }

    /**
     * 兼容 bizType / type 两种参数名
     * 优先级：bizType > type > defaultValue
     */
    private MediaBizType resolveBizType(MediaBizType bizType, MediaBizType type, MediaBizType defaultValue) {
        if (bizType != null) return bizType;
        if (type != null) return type;
        return defaultValue;
    }
}
