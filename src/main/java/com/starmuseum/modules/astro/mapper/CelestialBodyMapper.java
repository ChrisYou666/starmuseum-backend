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

    @Select("""
        SELECT
            b.id,
            b.catalog_code AS catalogCode,
            b.body_type AS bodyType,
            b.name,
            b.name_zh AS nameZh,
            b.name_en AS nameEn,
            b.name_id AS nameId,
            b.mag,
            b.constellation,
            b.wiki_url AS wikiUrl
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
        ORDER BY b.mag ASC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<BodySearchItemVO> search(@Param("catalogVersionCode") String catalogVersionCode,
                                  @Param("q") String q,
                                  @Param("limit") int limit,
                                  @Param("offset") long offset);
}
