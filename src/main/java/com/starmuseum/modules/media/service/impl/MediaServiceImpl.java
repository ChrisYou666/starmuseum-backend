package com.starmuseum.modules.media.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.modules.media.dto.MediaDTO;
import com.starmuseum.modules.media.dto.MediaUploadResponse;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.mapper.MediaMapper;
import com.starmuseum.modules.media.service.ExifSanitizer;
import com.starmuseum.modules.media.service.MediaService;
import com.starmuseum.modules.media.service.model.ExifSanitizeResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 说明：
 * - 本地存储：保存到 {user.dir}/uploads/yyyy/MM/dd/
 * - 3.3：上传后必须产出 sanitized 文件（去 EXIF）并且 URL 永远指向 sanitized
 * - thumb/medium 暂时不做缩放（直接复制 sanitized 一份）
 */
@Service
public class MediaServiceImpl extends ServiceImpl<MediaMapper, Media> implements MediaService {

    private static final int MAX_BATCH = 9;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String UPLOAD_DIR = "uploads";

    private final ExifSanitizer exifSanitizer;

    public MediaServiceImpl(ExifSanitizer exifSanitizer) {
        this.exifSanitizer = exifSanitizer;
    }

    @Override
    public MediaUploadResponse uploadOne(MultipartFile file, MediaBizType bizType) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("file is empty");
        }
        if (bizType == null) {
            bizType = MediaBizType.POST;
        }

        Long userId = currentUserId();

        // 1) 保存并清理 EXIF（关键：最终落盘必须是 sanitized）
        StoredFiles stored = storeLocalAndSanitize(file);

        // 2) 基础信息
        String mimeType = file.getContentType();
        long sizeBytes = file.getSize();

        // 3) 写入 media 表
        Media media = new Media();
        media.setUserId(userId);
        media.setBizType(bizType.name());

        media.setOriginUrl(stored.originUrl);
        media.setThumbUrl(stored.thumbUrl);
        media.setMediumUrl(stored.mediumUrl);

        media.setMimeType(mimeType);
        media.setSizeBytes(sizeBytes);

        media.setWidth(null);
        media.setHeight(null);

        media.setSha256(null);
        media.setStorageType("LOCAL");
        media.setStorageKey(stored.storageKey);
        media.setCreatedAt(LocalDateTime.now());

        // === 3.3 EXIF 字段写入 ===
        ExifSanitizeResult r = stored.exifResult;
        if (r != null) {
            media.setExifStripped(r.isExifStripped() ? 1 : 0);
            media.setExifHasGps(r.isExifHasGps() ? 1 : 0);
            media.setExifHasDevice(r.isExifHasDevice() ? 1 : 0);
            media.setExifCheckedAt(r.getCheckedAt());
        } else {
            // 理论上不会发生：storeLocalAndSanitize 保证 sanitize 执行
            media.setExifStripped(1);
            media.setExifHasGps(0);
            media.setExifHasDevice(0);
            media.setExifCheckedAt(LocalDateTime.now());
        }

        this.save(media);

        // 4) 返回
        MediaUploadResponse resp = new MediaUploadResponse();
        resp.setId(media.getId());
        resp.setBizType(media.getBizType());
        resp.setOriginUrl(media.getOriginUrl());
        resp.setThumbUrl(media.getThumbUrl());
        resp.setMediumUrl(media.getMediumUrl());
        resp.setMimeType(media.getMimeType());
        resp.setSizeBytes(media.getSizeBytes());
        resp.setWidth(media.getWidth());
        resp.setHeight(media.getHeight());
        resp.setCreatedAt(media.getCreatedAt());

        // === 3.3 response 输出 ===
        resp.setExifStripped(media.getExifStripped() != null && media.getExifStripped() == 1);
        resp.setExifHasGps(media.getExifHasGps() != null && media.getExifHasGps() == 1);
        resp.setExifHasDevice(media.getExifHasDevice() != null && media.getExifHasDevice() == 1);

        return resp;
    }

    @Override
    public List<MediaUploadResponse> uploadBatch(List<MultipartFile> files, MediaBizType bizType) {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("files is empty");
        }
        if (files.size() > MAX_BATCH) {
            throw new RuntimeException("最多只能上传 " + MAX_BATCH + " 张图片");
        }

        List<MediaUploadResponse> list = new ArrayList<>();
        for (MultipartFile f : files) {
            list.add(uploadOne(f, bizType));
        }
        return list;
    }

    @Override
    public MediaDTO getDtoById(Long id) {
        if (id == null) throw new RuntimeException("id is null");

        Media media = super.getById(id);
        if (media == null) {
            throw new RuntimeException("media not found: " + id);
        }

        Long uid = currentUserId();
        if (!Objects.equals(uid, media.getUserId())) {
            throw new AccessDeniedException("无权访问该资源");
        }

        MediaDTO dto = new MediaDTO();
        dto.setId(media.getId());
        dto.setUserId(media.getUserId());
        dto.setBizType(media.getBizType());
        dto.setOriginUrl(media.getOriginUrl());
        dto.setThumbUrl(media.getThumbUrl());
        dto.setMediumUrl(media.getMediumUrl());
        dto.setMimeType(media.getMimeType());
        dto.setSizeBytes(media.getSizeBytes());
        dto.setWidth(media.getWidth());
        dto.setHeight(media.getHeight());
        dto.setCreatedAt(media.getCreatedAt());
        return dto;
    }

    @Override
    public IPage<MediaDTO> pageMy(long page, long size, MediaBizType bizType) {
        Long uid = currentUserId();

        Page<Media> p = new Page<>(page, size);

        IPage<Media> entityPage = this.lambdaQuery()
            .eq(Media::getUserId, uid)
            .eq(bizType != null, Media::getBizType, bizType.name())
            .orderByDesc(Media::getId)
            .page(p);

        Page<MediaDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<MediaDTO> dtoRecords = new ArrayList<>();
        for (Media m : entityPage.getRecords()) {
            dtoRecords.add(toDTO(m));
        }
        dtoPage.setRecords(dtoRecords);
        return dtoPage;
    }

    @Override
    public void deleteMy(Long id) {
        if (id == null) throw new RuntimeException("id is null");

        Media media = super.getById(id);
        if (media == null) {
            throw new RuntimeException("media not found: " + id);
        }

        Long uid = currentUserId();
        if (!Objects.equals(uid, media.getUserId())) {
            throw new AccessDeniedException("无权删除该资源");
        }

        super.removeById(id);
    }

    // =========================
    // 内部方法
    // =========================

    private MediaDTO toDTO(Media media) {
        MediaDTO dto = new MediaDTO();
        dto.setId(media.getId());
        dto.setUserId(media.getUserId());
        dto.setBizType(media.getBizType());
        dto.setOriginUrl(media.getOriginUrl());
        dto.setThumbUrl(media.getThumbUrl());
        dto.setMediumUrl(media.getMediumUrl());
        dto.setMimeType(media.getMimeType());
        dto.setSizeBytes(media.getSizeBytes());
        dto.setWidth(media.getWidth());
        dto.setHeight(media.getHeight());
        dto.setCreatedAt(media.getCreatedAt());
        return dto;
    }

    private static class StoredFiles {
        String storageKey;
        String originUrl;
        String thumbUrl;
        String mediumUrl;

        // 3.3：保存 EXIF 检测结果
        ExifSanitizeResult exifResult;
    }

    /**
     * 3.3：保存 raw 临时文件 -> sanitize 输出 origin -> 删除 raw
     * thumb/medium 从 sanitized origin 复制
     */
    private StoredFiles storeLocalAndSanitize(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String day = LocalDate.now().format(DAY_FMT); // yyyy/MM/dd
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // raw 临时文件名（不对外暴露）
        String rawName = uuid + "_raw" + ext;

        // sanitized 文件名（最终对外）
        String originName = uuid + ext;
        String thumbName = uuid + "_t" + ext;
        String mediumName = uuid + "_m" + ext;

        String storageKey = day + "/" + originName;

        Path baseDir = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR, day);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("create upload dir failed", e);
        }

        Path rawPath = baseDir.resolve(rawName);
        Path originPath = baseDir.resolve(originName);
        Path thumbPath = baseDir.resolve(thumbName);
        Path mediumPath = baseDir.resolve(mediumName);

        // 1) 先落盘 raw
        try {
            Files.copy(file.getInputStream(), rawPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("store raw file failed", e);
        }

        // 2) sanitize(raw -> origin) （输出天然无 EXIF）
        ExifSanitizeResult exifResult;
        try {
            String outputFormat = guessFormatByExt(ext);
            exifResult = exifSanitizer.sanitize(rawPath, originPath, outputFormat);
        } catch (Exception e) {
            // 合规策略：sanitize 失败就拒绝上传，并清理 raw
            safeDelete(rawPath);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }

        // 3) 删除 raw，避免未经清理的文件落盘
        safeDelete(rawPath);

        // 4) thumb/medium 从 sanitized origin 复制（阶段3暂不缩放）
        try {
            Files.copy(originPath, thumbPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(originPath, mediumPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // 失败就尽量清理，避免遗留半成品
            safeDelete(originPath);
            safeDelete(thumbPath);
            safeDelete(mediumPath);
            throw new RuntimeException("create thumb/medium failed", e);
        }

        String baseUrl = "http://localhost:8080/" + UPLOAD_DIR + "/" + day + "/";

        StoredFiles s = new StoredFiles();
        s.storageKey = storageKey;
        s.originUrl = baseUrl + originName;
        s.thumbUrl = baseUrl + thumbName;
        s.mediumUrl = baseUrl + mediumName;
        s.exifResult = exifResult;
        return s;
    }

    private void safeDelete(Path p) {
        try {
            if (p != null) Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
    }

    private String guessFormatByExt(String ext) {
        if (!StringUtils.hasText(ext)) return "jpg";
        String e = ext.toLowerCase().trim();
        if (e.startsWith(".")) e = e.substring(1);
        if (e.equals("jpeg")) return "jpg";
        if (e.equals("jpg") || e.equals("png")) return e;
        // 阶段3：只允许 jpg/png 最稳
        return e;
    }

    /**
     * 获取当前登录用户 id（反射兜底，适配 JWT principal）
     */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("未登录");
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        if (principal instanceof String) {
            String s = (String) principal;
            try {
                return Long.parseLong(s);
            } catch (Exception ignored) {
            }
        }

        try {
            Method m = principal.getClass().getMethod("getId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {
        }

        try {
            Method m = principal.getClass().getMethod("getUserId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {
        }

        try {
            Method m = principal.getClass().getMethod("getUsername");
            Object v = m.invoke(principal);
            if (v != null) {
                return Long.parseLong(v.toString());
            }
        } catch (Exception ignored) {
        }

        throw new AccessDeniedException("无法解析当前用户ID，请在 currentUserId() 里适配你的 principal 类型");
    }
}
