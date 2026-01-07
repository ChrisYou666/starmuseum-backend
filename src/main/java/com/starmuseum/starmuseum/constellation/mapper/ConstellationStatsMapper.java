package com.starmuseum.starmuseum.constellation.mapper;

import com.starmuseum.starmuseum.constellation.dto.ConstellationOptionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConstellationStatsMapper {

    @Select("""
        SELECT
            constellation_code AS constellationCode,
            constellation_name AS constellationName,
            COUNT(*) AS lineCount
        FROM constellation_line
        WHERE is_deleted = 0
          AND constellation_code IS NOT NULL
          AND constellation_code <> ''
        GROUP BY constellation_code, constellation_name
        ORDER BY constellation_code ASC
        """)
    List<ConstellationOptionResponse> selectConstellationOptions();
}
