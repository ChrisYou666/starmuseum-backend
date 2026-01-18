package com.starmuseum.modules.catalog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.common.security.CurrentUser;
import com.starmuseum.modules.astro.entity.CatalogVersion;
import com.starmuseum.modules.astro.entity.CelestialAlias;
import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.mapper.CatalogVersionMapper;
import com.starmuseum.modules.astro.mapper.CelestialAliasMapper;
import com.starmuseum.modules.astro.mapper.CelestialBodyMapper;
import com.starmuseum.modules.catalog.config.CatalogStorageProperties;
import com.starmuseum.modules.catalog.dto.CatalogImportResponse;
import com.starmuseum.modules.catalog.dto.CatalogManifestDTO;
import com.starmuseum.modules.catalog.dto.CatalogObjectDTO;
import com.starmuseum.modules.catalog.service.CatalogImportService;
import com.starmuseum.modules.catalog.util.ChecksumsFileParser;
import com.starmuseum.modules.catalog.util.SafeZip;
import com.starmuseum.modules.catalog.util.Sha256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Phase 4.1 Catalog Import + Phase 5D extra_json
 *
 * 导入原则：
 * - 版本隔离写入（celestial_body.catalog_version_code = manifest.catalogVersion）
 * - 激活只切 active 指针（sys_kv_config.active_catalog_version），不改数据
 */
@Service
@RequiredArgsConstructor
public class CatalogImportServiceImpl implements CatalogImportService {

    private final CatalogStorageProperties props;
    private final ObjectMapper objectMapper;

    private final CatalogVersionMapper catalogVersionMapper;
    private final CelestialBodyMapper bodyMapper;
    private final CelestialAliasMapper aliasMapper;

    private final PlatformTransactionManager txManager;

    @Override
    public CatalogImportResponse importZip(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "file 不能为空");
        }

        Path tmpDir = prepareTempDir();
        Path extracted;
        try (InputStream in = new BufferedInputStream(file.getInputStream())) {
            extracted = SafeZip.unzip(in, tmpDir);
        } catch (Exception e) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "解压 zip 失败: " + e.getMessage());
        }

        Path manifestPath = findFirst(extracted, "manifest.json");
        if (manifestPath == null) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "缺少 manifest.json");
        }

        Path checksumsPath = findFirst(extracted, "checksums.sha256");
        if (checksumsPath == null) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "缺少 checksums.sha256");
        }

        Path objectsJsonl = findFirst(extracted, "objects.jsonl");
        Path objectsJson = findFirst(extracted, "objects.json");
        Path objectsPath = (objectsJsonl != null) ? objectsJsonl : objectsJson;
        if (objectsPath == null) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "缺少 objects.jsonl 或 objects.json（MVP 只要求 objects）");
        }

        CatalogManifestDTO manifest;
        String manifestRaw;
        try {
            manifestRaw = stripUtf8Bom(Files.readString(manifestPath, StandardCharsets.UTF_8)).trim();
            manifest = objectMapper.readValue(manifestRaw, CatalogManifestDTO.class);

        } catch (Exception e) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "manifest.json 解析失败: " + e.getMessage());
        }

        if (manifest == null || !StringUtils.hasText(manifest.getCatalogVersion())) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "manifest.catalogVersion 不能为空");
        }

        String versionCode = manifest.getCatalogVersion().trim();

        Map<String, String> expected;
        try (InputStream in = Files.newInputStream(checksumsPath)) {
            expected = ChecksumsFileParser.parse(in);
        } catch (Exception e) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "checksums.sha256 解析失败: " + e.getMessage());
        }

        String manifestKey = relativizeZipPath(extracted, manifestPath);
        String objectsKey = relativizeZipPath(extracted, objectsPath);

        verifyFileSha256(extracted, expected, manifestKey);
        verifyFileSha256(extracted, expected, objectsKey);

        List<CatalogObjectDTO> objects = readObjects(objectsPath);
        if (objects.isEmpty()) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "objects 为空");
        }

        List<String> preIssues = preValidateObjects(objects);
        if (!preIssues.isEmpty()) {
            cleanupQuietly(tmpDir);
            throw new BizException(400, "objects 预校验失败: " + String.join("; ", preIssues));
        }

        TransactionTemplate tt = new TransactionTemplate(txManager);

        try {
            CatalogImportResponse resp = tt.execute(status -> doImportTx(versionCode, manifest, manifestRaw, objects));
            cleanupQuietly(tmpDir);
            return resp;
        } catch (RuntimeException e) {
            try {
                tt.executeWithoutResult(status -> markFailed(versionCode, e.getMessage()));
            } catch (Exception ignored) {
            }
            cleanupQuietly(tmpDir);
            throw e;
        }
    }

    private CatalogImportResponse doImportTx(String versionCode,
                                             CatalogManifestDTO manifest,
                                             String manifestRaw,
                                             List<CatalogObjectDTO> objects) {

        CatalogVersion existing = catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getCode, versionCode)
                .last("limit 1")
        );

        if (existing != null) {
            String st = existing.getStatus();
            if (StringUtils.hasText(st) && !st.equals("FAILED")) {
                throw new BizException(400, "catalogVersion 已存在且不可覆盖（仅允许覆盖 FAILED 版本）: " + versionCode + ", status=" + st);
            }

            bodyMapper.delete(new LambdaQueryWrapper<CelestialBody>()
                .eq(CelestialBody::getCatalogVersionCode, versionCode));
        }

        LocalDateTime now = LocalDateTime.now();

        String manifestChecksum = null;
        try {
            manifestChecksum = Sha256Util.sha256Hex(manifestRaw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        CatalogVersion row = (existing != null) ? existing : new CatalogVersion();
        row.setCode(versionCode);
        row.setSchemaVersion(StringUtils.hasText(manifest.getSchemaVersion()) ? manifest.getSchemaVersion().trim() : null);
        row.setStatus("IMPORTED");
        row.setManifestJson(manifestRaw);
        row.setManifestChecksum(manifestChecksum);
        row.setChecksum(manifestChecksum);
        row.setImportedAt(now);
        row.setLastError(null);

        if (StringUtils.hasText(manifest.getBuildTime())) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(manifest.getBuildTime().trim());
                row.setBuildTime(odt.toLocalDateTime());
            } catch (Exception ignored) {
            }
        }

        row.setImportedBy(CurrentUser.getUserIdOrNull());

        if (existing == null) {
            catalogVersionMapper.insert(row);
        } else {
            row.setUpdatedAt(now);
            catalogVersionMapper.updateById(row);
        }

        for (CatalogObjectDTO o : objects) {
            CelestialBody b = new CelestialBody();
            b.setCatalogVersionCode(versionCode);
            b.setCatalogCode(o.getCatalogCode().trim());
            b.setBodyType(StringUtils.hasText(o.getBodyType()) ? o.getBodyType().trim() : "STAR");
            b.setName(o.getName() == null ? null : o.getName().trim());
            b.setNameZh(trimOrNull(o.getNameZh()));
            b.setNameEn(trimOrNull(o.getNameEn()));
            b.setNameId(trimOrNull(o.getNameId()));
            b.setRaDeg(o.getRaDeg());
            b.setDecDeg(o.getDecDeg());
            b.setMag(o.getMag());
            b.setSpectralType(trimOrNull(o.getSpectralType()));
            b.setConstellation(trimOrNull(o.getConstellation()));
            b.setWikiUrl(trimOrNull(o.getWikiUrl()));

            // Phase 5D：写入扩展字段 extra_json（未知字段自动收集）
            b.setExtraJson(buildExtraJson(o));

            bodyMapper.insert(b);

            Long bodyId = b.getId();

            if (o.getAliases() != null) {
                for (CatalogObjectDTO.AliasItem ai : o.getAliases()) {
                    if (ai == null) continue;
                    if (!StringUtils.hasText(ai.getAliasName())) continue;

                    CelestialAlias a = new CelestialAlias();
                    a.setBodyId(bodyId);
                    a.setLang(StringUtils.hasText(ai.getLang()) ? ai.getLang().trim() : "und");
                    a.setAliasName(ai.getAliasName().trim());
                    aliasMapper.insert(a);
                }
            }
        }

        CatalogImportResponse resp = new CatalogImportResponse();
        resp.setCode(versionCode);
        resp.setStatus("IMPORTED");
        resp.setImportedAt(now);
        resp.setMessage("imported objects=" + objects.size());

        return resp;
    }

    private String buildExtraJson(CatalogObjectDTO o) {
        if (o == null) return null;
        Map<String, Object> extra = o.getExtra();
        if (extra == null || extra.isEmpty()) return null;

        // 过滤掉明显不该写入的字段（保险）
        extra.remove("catalogCode");
        extra.remove("bodyType");
        extra.remove("name");
        extra.remove("nameZh");
        extra.remove("nameEn");
        extra.remove("nameId");
        extra.remove("raDeg");
        extra.remove("decDeg");
        extra.remove("mag");
        extra.remove("spectralType");
        extra.remove("constellation");
        extra.remove("wikiUrl");
        extra.remove("aliases");

        if (extra.isEmpty()) return null;

        try {
            return objectMapper.writeValueAsString(extra);
        } catch (Exception e) {
            // 不因 extra 失败影响主流程
            return null;
        }
    }

    private void markFailed(String versionCode, String message) {
        if (!StringUtils.hasText(versionCode)) return;

        CatalogVersion row = catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getCode, versionCode)
                .last("limit 1")
        );

        LocalDateTime now = LocalDateTime.now();

        if (row == null) {
            CatalogVersion ins = new CatalogVersion();
            ins.setCode(versionCode);
            ins.setStatus("FAILED");
            ins.setLastError(safeError(message));
            ins.setImportedAt(now);
            catalogVersionMapper.insert(ins);
            return;
        }

        CatalogVersion upd = new CatalogVersion();
        upd.setId(row.getId());
        upd.setStatus("FAILED");
        upd.setLastError(safeError(message));
        upd.setUpdatedAt(now);
        catalogVersionMapper.updateById(upd);
    }

    private List<CatalogObjectDTO> readObjects(Path objectsPath) {
        String name = objectsPath.getFileName().toString().toLowerCase();
        try {
            if (name.endsWith(".jsonl")) {
                List<CatalogObjectDTO> list = new ArrayList<>();
                List<String> lines = Files.readAllLines(objectsPath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    String s = line == null ? "" : stripUtf8Bom(line).trim();
                    if (s.isEmpty()) continue;
                    CatalogObjectDTO o = objectMapper.readValue(s, CatalogObjectDTO.class);
                    if (o != null) list.add(o);
                }
                return list;
            }

            String raw = Files.readString(objectsPath, StandardCharsets.UTF_8);
            return objectMapper.readValue(raw, new TypeReference<List<CatalogObjectDTO>>() {});
        } catch (Exception e) {
            throw new BizException(400, "objects 解析失败: " + e.getMessage());
        }
    }

    private List<String> preValidateObjects(List<CatalogObjectDTO> objects) {
        List<String> issues = new ArrayList<>();

        Set<String> codes = new HashSet<>();
        Map<String, String> aliasOwner = new HashMap<>(); // key=lang|aliasName -> catalogCode

        int idx = 0;
        for (CatalogObjectDTO o : objects) {
            idx++;

            if (o == null) {
                issues.add("objects[" + idx + "] is null");
                continue;
            }

            if (!StringUtils.hasText(o.getCatalogCode())) {
                issues.add("objects[" + idx + "].catalogCode 不能为空");
                continue;
            }

            String code = o.getCatalogCode().trim();
            if (!codes.add(code)) {
                issues.add("重复 catalogCode: " + code);
            }

            if (!StringUtils.hasText(o.getName())) {
                issues.add("catalogCode=" + code + " 的 name 不能为空");
            }

            if (o.getRaDeg() != null) {
                if (o.getRaDeg() < 0 || o.getRaDeg() >= 360) {
                    issues.add("catalogCode=" + code + " raDeg 超范围 [0,360): " + o.getRaDeg());
                }
            }
            if (o.getDecDeg() != null) {
                if (o.getDecDeg() < -90 || o.getDecDeg() > 90) {
                    issues.add("catalogCode=" + code + " decDeg 超范围 [-90,90]: " + o.getDecDeg());
                }
            }
            if (o.getMag() != null) {
                if (o.getMag() < -30 || o.getMag() > 40) {
                    issues.add("catalogCode=" + code + " mag 超范围 [-30,40]: " + o.getMag());
                }
            }

            if (o.getAliases() != null) {
                for (CatalogObjectDTO.AliasItem ai : o.getAliases()) {
                    if (ai == null) continue;
                    if (!StringUtils.hasText(ai.getAliasName())) continue;
                    String lang = StringUtils.hasText(ai.getLang()) ? ai.getLang().trim() : "und";
                    String an = ai.getAliasName().trim().toLowerCase();
                    String k = lang + "|" + an;
                    String owner = aliasOwner.putIfAbsent(k, code);
                    if (owner != null && !owner.equals(code)) {
                        issues.add("别名冲突: lang=" + lang + ", alias=" + ai.getAliasName().trim()
                            + " 同时属于 " + owner + " 和 " + code);
                    }
                }
            }
        }

        return issues;
    }

    private void verifyFileSha256(Path root, Map<String, String> expected, String relPath) {
        String exp = expected.get(relPath);
        if (!StringUtils.hasText(exp)) {
            throw new BizException(400, "checksums.sha256 未包含文件: " + relPath);
        }

        Path file = root.resolve(relPath).normalize();
        if (!Files.exists(file)) {
            throw new BizException(400, "文件不存在: " + relPath);
        }

        try (InputStream in = Files.newInputStream(file)) {
            String act = Sha256Util.sha256Hex(in);
            if (!exp.equalsIgnoreCase(act)) {
                throw new BizException(400, "sha256 校验失败: " + relPath + " expected=" + exp + " actual=" + act);
            }
        } catch (IOException e) {
            throw new BizException(400, "读取文件失败: " + relPath);
        }
    }

    private Path prepareTempDir() {
        String baseDir = props.getTempDir();
        if (!StringUtils.hasText(baseDir)) {
            baseDir = System.getProperty("java.io.tmpdir");
        }

        String name = "starmuseum-catalog-import-" + UUID.randomUUID();
        Path dir = Paths.get(baseDir, name);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BizException(500, "创建临时目录失败: " + dir);
        }
        return dir;
    }

    private Path findFirst(Path root, String filename) {
        try {
            try (var s = Files.walk(root)) {
                return s.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().equalsIgnoreCase(filename))
                    .findFirst()
                    .orElse(null);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private String relativizeZipPath(Path root, Path file) {
        Path rel = root.relativize(file);
        return rel.toString().replace('\\', '/');
    }

    private void cleanupQuietly(Path dir) {
        if (dir == null) return;
        try {
            if (!Files.exists(dir)) return;
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
        } catch (Exception ignored) {
        }
    }

    private String trimOrNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private String safeError(String msg) {
        if (msg == null) return null;
        msg = msg.trim();
        if (msg.length() <= 1000) return msg;
        return msg.substring(0, 1000);
    }

    private String stripUtf8Bom(String s) {
        if (s == null || s.isEmpty()) return s;

        if (s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }

        if (s.startsWith("ï»¿")) {
            return s.substring(3);
        }

        return s;
    }
}
