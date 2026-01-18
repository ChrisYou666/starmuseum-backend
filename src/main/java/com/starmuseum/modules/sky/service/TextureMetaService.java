package com.starmuseum.modules.sky.service;

import com.starmuseum.common.config.CatalogProperties;
import com.starmuseum.modules.sky.vo.TextureLevelVO;
import com.starmuseum.modules.sky.vo.TextureMetaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextureMetaService {

    private final CatalogProperties catalogProperties;

    /**
     * 获取某个贴图场景（如 milkyway）的分档元数据
     *
     * scene 目录结构：
     * {storageDir}/textures/{scene}/{resolution}.{ext}
     * 例：
     * D:/data/starmuseum/catalog_storage/textures/milkyway/2k.webp
     */
    public TextureMetaResponse getTextureMeta(String scene) {
        if (!StringUtils.hasText(scene)) {
            scene = "milkyway";
        }
        scene = scene.trim();

        String storageDir = catalogProperties.getStorageDir();
        if (!StringUtils.hasText(storageDir)) {
            throw new IllegalStateException("catalog.storage-dir 未配置");
        }

        List<String> resolutions = catalogProperties.getTextures().getResolutions();
        if (resolutions == null || resolutions.isEmpty()) {
            resolutions = List.of("2k", "4k", "8k");
        }

        List<String> exts = catalogProperties.getTextures().getExtensions();
        if (exts == null || exts.isEmpty()) {
            exts = List.of("webp", "jpg", "jpeg", "png");
        }

        String base = catalogProperties.effectiveResourceBaseUrl(); // 例如 /catalog
        String baseUrl = catalogProperties.effectiveBaseUrl();      // 例如 http://localhost:8080

        Path root = Path.of(storageDir).toAbsolutePath().normalize();
        Path sceneDir = root.resolve("textures").resolve(scene).normalize();

        List<TextureLevelVO> levels = new ArrayList<>();
        for (String res : resolutions) {
            TextureLevelVO vo = new TextureLevelVO();
            vo.setResolution(res);

            // 按 extensions 顺序找第一个存在的文件
            Path found = null;
            String foundExt = null;
            for (String ext : exts) {
                String fileName = res + "." + ext;
                Path p = sceneDir.resolve(fileName).normalize();
                if (Files.exists(p) && Files.isRegularFile(p)) {
                    found = p;
                    foundExt = ext;
                    break;
                }
            }

            if (found == null) {
                vo.setExists(false);
                vo.setPath(base + "/textures/" + scene + "/" + res + "." + exts.get(0)); // 给一个默认形状
                vo.setUrl(baseUrl != null ? baseUrl + vo.getPath() : vo.getPath());
                vo.setSizeBytes(null);
                vo.setSha256(null);
                levels.add(vo);
                continue;
            }

            vo.setExists(true);

            String relative = base + "/textures/" + scene + "/" + res + "." + foundExt;
            vo.setPath(relative);
            vo.setUrl(baseUrl != null ? baseUrl + relative : relative);

            try {
                vo.setSizeBytes(Files.size(found));
            } catch (Exception e) {
                vo.setSizeBytes(null);
            }

            // sha256
            try (InputStream in = Files.newInputStream(found)) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) {
                    md.update(buf, 0, n);
                }
                vo.setSha256(HexFormat.of().formatHex(md.digest()));
            } catch (Exception e) {
                vo.setSha256(null);
            }

            levels.add(vo);
        }

        TextureMetaResponse resp = new TextureMetaResponse();
        resp.setScene(scene);
        resp.setResourceBaseUrl(base);
        resp.setStorageDir(root.toString());
        resp.setLevels(levels);
        return resp;
    }
}
