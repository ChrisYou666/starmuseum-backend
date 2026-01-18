package com.starmuseum.modules.sky.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starmuseum.common.config.CatalogProperties;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.sky.vo.CatalogActiveResponse;
import com.starmuseum.modules.sky.vo.SkyObjectItemVO;
import com.starmuseum.modules.sky.vo.SkyObjectsResponse;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Phase 4.4：
 * - /api/sky/catalog/active
 * - /api/sky/objects?version=active&type=STAR&limit=...
 *
 * 同时支持两种数据源：
 * 1) DB（默认）：通过 JdbcTemplate 尝试几套常见表结构（兼容现有库）
 * 2) FILE：从 {catalog.storage-dir}/objects/{type}.json 读取
 *
 * 切换方式（不改代码）：
 * starmuseum.sky.objects.source = db | file
 */
@Service
public class SkyCatalogQueryService {

    private final CatalogProperties catalogProperties;
    private final Environment environment;

    /**
     * 允许为 null：当你选择 file 模式或项目未启用 datasource 时，也能启动
     */
    @Nullable
    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ✅ 只保留一个构造器（关键修复点）
     * Spring Boot 会自动用这个构造器注入依赖（无需 @Autowired）
     */
    public SkyCatalogQueryService(CatalogProperties catalogProperties,
                                  Environment environment,
                                  @Nullable JdbcTemplate jdbcTemplate) {
        this.catalogProperties = catalogProperties;
        this.environment = environment;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * active catalog 元信息
     */
    public CatalogActiveResponse getActiveCatalog() {
        CatalogActiveResponse resp = new CatalogActiveResponse();

        resp.setStorageDir(Path.of(catalogProperties.getStorageDir())
            .toAbsolutePath()
            .normalize()
            .toString());

        // 兼容：public-path 没配时兜底 /catalog
        String resourceBase = getResourceBaseUrlSafe();
        resp.setResourceBaseUrl(resourceBase);

        // 尝试从 DB 查询 active 版本（失败则降级）
        String activeVersion = tryQueryActiveVersionFromDb();
        resp.setActiveVersion(activeVersion);

        LocalDateTime activatedAt = tryQueryActivatedAtFromDb();
        resp.setActivatedAt(activatedAt);

        return resp;
    }

    /**
     * objects 按需加载
     */
    public SkyObjectsResponse getObjects(String version, String type, Integer limit) {
        if (!StringUtils.hasText(type)) {
            type = "STAR";
        }
        type = type.trim().toUpperCase(Locale.ROOT);

        if (!StringUtils.hasText(version)) {
            version = "active";
        }
        version = version.trim();

        if (limit == null || limit <= 0) {
            limit = 200;
        }
        if (limit > 5000) {
            limit = 5000;
        }

        String realVersion = version;
        if ("active".equalsIgnoreCase(version)) {
            realVersion = tryQueryActiveVersionFromDb();
            if (!StringUtils.hasText(realVersion)) {
                realVersion = "active";
            }
        }

        String source = environment.getProperty("starmuseum.sky.objects.source", "db")
            .trim()
            .toLowerCase(Locale.ROOT);

        List<SkyObjectItemVO> items;

        if ("file".equals(source)) {
            items = loadObjectsFromFile(type, limit);
        } else {
            items = loadObjectsFromDb(realVersion, type, limit);
            if (items == null) {
                throw new BizException(500,
                    "未能从数据库读取星表数据：请确认星表相关表结构/表名。也可切换 file 模式：starmuseum.sky.objects.source=file");
            }
        }

        SkyObjectsResponse resp = new SkyObjectsResponse();
        resp.setVersion(version);
        resp.setType(type);
        resp.setItems(items);
        resp.setCount(items == null ? 0 : items.size());
        return resp;
    }

    // ===================== helpers =====================

    private String getResourceBaseUrlSafe() {
        // 你的配置里是 catalog.public-path: /catalog
        try {
            // CatalogProperties 里大概率有 getPublicPath()
            String v = catalogProperties.getPublicPath();
            if (StringUtils.hasText(v)) return v;
        } catch (Exception ignored) {
        }
        // 如果你的 CatalogProperties 字段名不叫 publicPath，这里兜底不影响启动
        return "/catalog";
    }

    // ===================== DB：active version（多套 SQL 兼容） =====================

    private String tryQueryActiveVersionFromDb() {
        if (jdbcTemplate == null) {
            return "active";
        }

        List<String> candidates = List.of(
            "select version_code from catalog_version where is_active = 1 limit 1",
            "select version_code from catalog_version where status = 'ACTIVE' order by activated_at desc limit 1",
            "select active_version as version_code from catalog_active limit 1",
            "select v as version_code from catalog_kv where k='active_version' limit 1"
        );

        for (String sql : candidates) {
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                if (rows != null && !rows.isEmpty()) {
                    Object v = rows.get(0).get("version_code");
                    if (v != null && StringUtils.hasText(String.valueOf(v))) {
                        return String.valueOf(v);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return "active";
    }

    private LocalDateTime tryQueryActivatedAtFromDb() {
        if (jdbcTemplate == null) {
            return null;
        }

        List<String> candidates = List.of(
            "select activated_at from catalog_version where is_active = 1 limit 1",
            "select activated_at from catalog_version where status = 'ACTIVE' order by activated_at desc limit 1"
        );

        for (String sql : candidates) {
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                if (rows != null && !rows.isEmpty()) {
                    Object v = rows.get(0).get("activated_at");
                    if (v instanceof java.sql.Timestamp ts) {
                        return ts.toLocalDateTime();
                    }
                    if (v instanceof LocalDateTime ldt) {
                        return ldt;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // ===================== DB：objects（多套表结构兼容） =====================

    private List<SkyObjectItemVO> loadObjectsFromDb(String realVersion, String type, int limit) {
        if (jdbcTemplate == null) {
            return null;
        }

        List<String> candidates = new ArrayList<>();

        candidates.add("select id, type, name, ra_deg, dec_deg, mag from sky_object where type=? order by mag asc, id asc limit ?");
        candidates.add("select id, type, name, ra_deg, dec_deg, mag from sky_object where version_code=? and type=? order by mag asc, id asc limit ?");

        candidates.add("select id, type, name, ra_deg, dec_deg, mag from astro_body where type=? order by mag asc, id asc limit ?");
        candidates.add("select id, type, name, raDeg as ra_deg, decDeg as dec_deg, mag from astro_body where type=? order by mag asc, id asc limit ?");

        candidates.add("select id, 'STAR' as type, name, ra_deg, dec_deg, mag from star order by mag asc, id asc limit ?");

        for (String sql : candidates) {
            try {
                List<SkyObjectItemVO> items;

                if (sql.contains("where version_code=? and type=?")) {
                    items = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToItem(rs), realVersion, type, limit);
                } else if (sql.contains("where type=?") && sql.contains("limit ?")) {
                    items = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToItem(rs), type, limit);
                } else if (sql.contains("from star") && sql.contains("limit ?")) {
                    items = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToItem(rs), limit);
                } else {
                    continue;
                }

                if (items != null) {
                    return items;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private SkyObjectItemVO mapRowToItem(ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            Set<String> cols = new HashSet<>();
            for (int i = 1; i <= n; i++) {
                cols.add(md.getColumnLabel(i).toLowerCase(Locale.ROOT));
            }

            SkyObjectItemVO vo = new SkyObjectItemVO();

            if (cols.contains("id")) {
                Object id = rs.getObject("id");
                if (id != null) {
                    vo.setId(((Number) id).longValue());
                }
            }

            if (cols.contains("type")) {
                vo.setType(rs.getString("type"));
            } else {
                vo.setType("STAR");
            }

            String name = null;
            if (cols.contains("name")) name = rs.getString("name");
            if (!StringUtils.hasText(name) && cols.contains("en_name")) name = rs.getString("en_name");
            if (!StringUtils.hasText(name) && cols.contains("zh_name")) name = rs.getString("zh_name");
            vo.setName(name);

            vo.setRaDeg(getDouble(rs, cols, "ra_deg", "radeg", "ra"));
            vo.setDecDeg(getDouble(rs, cols, "dec_deg", "decdeg", "dec"));
            vo.setMag(getDouble(rs, cols, "mag", "magnitude"));

            return vo;
        } catch (Exception e) {
            throw new BizException(500, "星表数据映射失败: " + e.getMessage());
        }
    }

    private Double getDouble(ResultSet rs, Set<String> cols, String... keys) {
        for (String k : keys) {
            String kk = k.toLowerCase(Locale.ROOT);
            if (cols.contains(kk)) {
                try {
                    Object v = rs.getObject(k);
                    if (v == null) return null;
                    if (v instanceof Number num) return num.doubleValue();
                    String s = String.valueOf(v);
                    if (!StringUtils.hasText(s)) return null;
                    return Double.parseDouble(s);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    // ===================== FILE：objects =====================

    private List<SkyObjectItemVO> loadObjectsFromFile(String type, int limit) {
        String storageDir = catalogProperties.getStorageDir();
        if (!StringUtils.hasText(storageDir)) {
            throw new BizException(500, "catalog.storage-dir 未配置，无法使用 file 模式读取 objects");
        }

        String fileDir = environment.getProperty("starmuseum.sky.objects.file-dir",
            Path.of(storageDir).resolve("objects").toString());

        Path dir = Path.of(fileDir).toAbsolutePath().normalize();
        Path json = dir.resolve(type + ".json");
        Path ndjson = dir.resolve(type + ".ndjson");

        try {
            if (Files.exists(json)) {
                List<SkyObjectItemVO> all = objectMapper.readValue(Files.readAllBytes(json),
                    new TypeReference<List<SkyObjectItemVO>>() {});
                if (all == null) return List.of();
                return all.subList(0, Math.min(limit, all.size()));
            }

            if (Files.exists(ndjson)) {
                List<String> lines = Files.readAllLines(ndjson);
                List<SkyObjectItemVO> out = new ArrayList<>();
                for (String line : lines) {
                    if (!StringUtils.hasText(line)) continue;
                    out.add(objectMapper.readValue(line, SkyObjectItemVO.class));
                    if (out.size() >= limit) break;
                }
                return out;
            }

            return List.of();
        } catch (Exception e) {
            throw new BizException(500, "file 模式读取 objects 失败: " + e.getMessage());
        }
    }
}
