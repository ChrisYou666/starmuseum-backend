package com.starmuseum.modules.media.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.modules.media.dto.MediaDTO;
import com.starmuseum.modules.media.dto.MediaUploadResponse;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import com.starmuseum.modules.media.mapper.MediaMapper;
import com.starmuseum.modules.media.service.MediaService;
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
 * - 这里用“本地存储”实现上传：保存到 {user.dir}/uploads/yyyy/MM/dd/
 * - thumb/medium 暂时不做缩放（直接复制一份）
 * - B 模块默认只能操作自己的 media
 */
@Service
public class MediaServiceImpl extends ServiceImpl<MediaMapper, Media> implements MediaService {

    private static final int MAX_BATCH = 9;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public MediaUploadResponse uploadOne(MultipartFile file, MediaBizType bizType) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("file is empty");
        }
        if (bizType == null) {
            bizType = MediaBizType.POST;
        }

        Long userId = currentUserId();

        // 1) 保存文件到本地
        StoredFiles stored = storeLocal(file);

        // 2) 读基础信息
        String mimeType = file.getContentType();
        long sizeBytes = file.getSize();

        // 3) 写入 media 表（关键：userId + bizType）
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
    }

    private StoredFiles storeLocal(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String day = LocalDate.now().format(DAY_FMT); // yyyy/MM/dd
        String uuid = UUID.randomUUID().toString().replace("-", "");

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

        Path originPath = baseDir.resolve(originName);
        Path thumbPath = baseDir.resolve(thumbName);
        Path mediumPath = baseDir.resolve(mediumName);

        try {
            Files.copy(file.getInputStream(), originPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(originPath, thumbPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(originPath, mediumPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("store file failed", e);
        }

        String baseUrl = "http://localhost:8080/" + UPLOAD_DIR + "/" + day + "/";

        StoredFiles s = new StoredFiles();
        s.storageKey = storageKey;
        s.originUrl = baseUrl + originName;
        s.thumbUrl = baseUrl + thumbName;
        s.mediumUrl = baseUrl + mediumName;
        return s;
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
