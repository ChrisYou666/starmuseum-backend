package com.starmuseum.modules.astro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.vo.BodySearchItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CelestialBodyMapper extends BaseMapper<CelestialBody> {

    /**
     * Phase 5D：搜索增强
     * 目标：
     * - 支持 catalog_code / name / 多语言名 / alias_name 命中
     * - 排序更符合直觉：精确命中 > 前缀命中 > 包含命中；catalog_code 精确最高
     *
     * 说明：
     * - 这里用子查询聚合出 score，避免 LEFT JOIN 产生重复行导致排序不稳定
     */
    @Select("""
        SELECT
            t.id,
            t.catalogCode,
            t.bodyType,
            t.name,
            t.nameZh,
            t.nameEn,
            t.nameId,
            t.mag,
            t.constellation,
            t.wikiUrl,
            t.extraJson
        FROM (
            SELECT
                b.id AS id,
                b.catalog_code AS catalogCode,
                b.body_type AS bodyType,
                b.name AS name,
                b.name_zh AS nameZh,
                b.name_en AS nameEn,
                b.name_id AS nameId,
                b.mag AS mag,
                b.constellation AS constellation,
                b.wiki_url AS wikiUrl,
                b.extra_json AS extraJson,
                MIN(
                    CASE
                        -- 0: catalog_code 精确命中（最高优先）
                        WHEN LOWER(b.catalog_code) = LOWER(#{q}) THEN 0

                        -- 1: alias 精确命中
                        WHEN LOWER(a.alias_name) = LOWER(#{q}) THEN 1

                        -- 2: name 精确命中（含多语言）
                        WHEN LOWER(b.name)    = LOWER(#{q})
                          OR LOWER(b.name_zh) = LOWER(#{q})
                          OR LOWER(b.name_en) = LOWER(#{q})
                          OR LOWER(b.name_id) = LOWER(#{q}) THEN 2

                        -- 3: catalog_code 前缀命中
                        WHEN LOWER(b.catalog_code) LIKE CONCAT(LOWER(#{q}), '%') THEN 3

                        -- 4: alias 前缀命中
                        WHEN LOWER(a.alias_name) LIKE CONCAT(LOWER(#{q}), '%') THEN 4

                        -- 5: name 前缀命中
                        WHEN LOWER(b.name)    LIKE CONCAT(LOWER(#{q}), '%')
                          OR LOWER(b.name_zh) LIKE CONCAT(LOWER(#{q}), '%')
                          OR LOWER(b.name_en) LIKE CONCAT(LOWER(#{q}), '%')
                          OR LOWER(b.name_id) LIKE CONCAT(LOWER(#{q}), '%') THEN 5

                        -- 6: 包含命中（最后）
                        ELSE 6
                    END
                ) AS score
            FROM celestial_body b
            LEFT JOIN celestial_alias a ON a.body_id = b.id
            WHERE b.catalog_version_code = #{catalogVersionCode}
              AND (
                    b.catalog_code LIKE CONCAT('%', #{q}, '%')
                 OR b.name        LIKE CONCAT('%', #{q}, '%')
                 OR b.name_zh     LIKE CONCAT('%', #{q}, '%')
                 OR b.name_en     LIKE CONCAT('%', #{q}, '%')
                 OR b.name_id     LIKE CONCAT('%', #{q}, '%')
                 OR a.alias_name  LIKE CONCAT('%', #{q}, '%')
              )
            GROUP BY b.id
        ) t
        ORDER BY t.score ASC, t.mag ASC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<BodySearchItemVO> search(@Param("catalogVersionCode") String catalogVersionCode,
                                  @Param("q") String q,
                                  @Param("limit") int limit,
                                  @Param("offset") long offset);
}
