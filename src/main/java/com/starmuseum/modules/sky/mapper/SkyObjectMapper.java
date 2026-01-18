package com.starmuseum.modules.sky.mapper;

import com.starmuseum.modules.sky.vo.SkyObjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SkyObjectMapper {

    /**
     * 查询指定版本、指定类型的天体列表（按星等 mag 升序，NULL 放最后）
     *
     * 表：celestial_body
     * 字段映射：
     * - catalog_version_code -> catalogVersion
     * - body_type            -> type
     * - ra_deg               -> raDeg
     * - dec_deg              -> decDeg
     */
    @Select("""
        SELECT
            id,
            name,
            body_type AS type,
            ra_deg AS raDeg,
            dec_deg AS decDeg,
            mag
        FROM celestial_body
        WHERE catalog_version_code = #{catalogVersion}
          AND body_type = #{type}
        ORDER BY
          CASE WHEN mag IS NULL THEN 1 ELSE 0 END ASC,
          mag ASC,
          id ASC
        LIMIT #{limit}
        """)
    List<SkyObjectVO> selectObjectsByType(
        @Param("catalogVersion") String catalogVersion,
        @Param("type") String type,
        @Param("limit") int limit
    );
}
