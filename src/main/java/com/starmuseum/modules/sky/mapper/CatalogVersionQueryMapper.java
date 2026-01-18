package com.starmuseum.modules.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CatalogVersionQueryMapper {

    @Select("""
        SELECT code
        FROM catalog_version
        WHERE status = 'ACTIVE'
        ORDER BY activated_at DESC, id DESC
        LIMIT 1
        """)
    String findActiveCatalogCode();
}
