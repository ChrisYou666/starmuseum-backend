package com.starmuseum.modules.sky.service;

import com.starmuseum.common.config.CatalogStorageProperties;
import com.starmuseum.modules.sky.vo.TextureMetadataResponse;
import com.starmuseum.modules.sky.vo.TextureVariantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextureMetadataService {

    private final CatalogStorageProperties props;

    public TextureMetadataResponse getTextureMetadata(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("key 不能为空");
        }

        String baseUrl = normalizeBaseUrl(props.getResourceBaseUrl());
        Path storageRoot = Path.of(props.getStorageDir()).toAbsolutePath().normalize();

        // textures/{key}/{resolution}.{ext}
        Path textureDir = storageRoot.resolve("textures").resolve(key).normalize();

        List<TextureVariantVO> variants = new ArrayList<>();
        for (String res : props.getTextures().getResolutions()) {
            TextureVariantVO v = new TextureVariantVO();
            v.setResolution(res);

            FoundFile found = findFirstExisting(textureDir, res, props.getTextures().getExtensions());
            if (found == null) {
                v.setAvailable(false);
            } else {
                v.setAvailable(true);
                v.setSizeBytes(found.sizeBytes);
                v.setEtag(found.etag);
                // url = /catalog + /textures/{key}/{filename}
                v.setUrl(baseUrl + "/textures/" + key + "/" + found.fileName);
            }
            variants.add(v);
        }

        TextureMetadataResponse resp = new TextureMetadataResponse();
        resp.setKey(key);
        resp.setVariants(variants);
        return resp;
    }

    private FoundFile findFirstExisting(Path textureDir, String resolution, List<String> exts) {
        for (String ext : exts) {
            String fileName = resolution + "." + ext;
            Path p = textureDir.resolve(fileName);
            if (Files.exists(p) && Files.isRegularFile(p)) {
                try {
                    long size = Files.size(p);
                    long lm = Files.getLastModifiedTime(p).toMillis();
                    // 简易 ETag：size-lastModified（足够用于前端缓存对比）
                    String etag = size + "-" + lm;
                    return new FoundFile(fileName, size, etag);
                } catch (Exception ignore) {
                    // 读不到就当不可用
                    return null;
                }
            }
        }
        return null;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) return "/catalog";
        String v = baseUrl.trim();
        if (!v.startsWith("/")) v = "/" + v;
        if (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }

    private static class FoundFile {
        final String fileName;
        final long sizeBytes;
        final String etag;

        FoundFile(String fileName, long sizeBytes, String etag) {
            this.fileName = fileName;
            this.sizeBytes = sizeBytes;
            this.etag = etag;
        }
    }
}
