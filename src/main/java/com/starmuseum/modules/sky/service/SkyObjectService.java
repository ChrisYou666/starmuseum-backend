package com.starmuseum.modules.sky.service;

import com.starmuseum.modules.sky.mapper.CatalogVersionQueryMapper;
import com.starmuseum.modules.sky.mapper.SkyObjectMapper;
import com.starmuseum.modules.sky.vo.SkyObjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkyObjectService {

    private final SkyObjectMapper skyObjectMapper;
    private final CatalogVersionQueryMapper catalogVersionQueryMapper;

    public List<SkyObjectVO> getObjects(String version, String type, Integer limit) {
        String resolvedVersion = resolveVersion(version);
        String resolvedType = resolveType(type);
        int resolvedLimit = resolveLimit(limit);

        return skyObjectMapper.selectObjectsByType(resolvedVersion, resolvedType, resolvedLimit);
    }

    private String resolveVersion(String version) {
        if (!StringUtils.hasText(version) || "active".equalsIgnoreCase(version)) {
            String active = catalogVersionQueryMapper.findActiveCatalogCode();
            if (!StringUtils.hasText(active)) {
                throw new IllegalStateException("当前没有 ACTIVE 的 catalog_version");
            }
            return active;
        }
        return version.trim();
    }

    private String resolveType(String type) {
        if (!StringUtils.hasText(type)) {
            return "STAR";
        }
        return type.trim().toUpperCase();
    }

    private int resolveLimit(Integer limit) {
        int v = (limit == null ? 200 : limit);
        if (v < 1) v = 1;
        if (v > 5000) v = 5000; // 防止一次拉太多
        return v;
    }
}
