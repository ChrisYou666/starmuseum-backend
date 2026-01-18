package com.starmuseum.modules.catalog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CatalogValidationMapper {

    @Select("""
        SELECT a.lang AS lang, a.alias_name AS aliasName, COUNT(*) AS cnt
        FROM celestial_alias a
        JOIN celestial_body b ON a.body_id = b.id
        WHERE b.catalog_version_code = #{versionCode}
        GROUP BY a.lang, a.alias_name
        HAVING COUNT(*) > 1
        ORDER BY cnt DESC
        LIMIT 20
        """)
    List<DuplicateAliasRow> findDuplicateAliases(@Param("versionCode") String versionCode);
}
