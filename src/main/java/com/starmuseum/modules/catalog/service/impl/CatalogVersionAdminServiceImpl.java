package com.starmuseum.modules.catalog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.astro.entity.CatalogVersion;
import com.starmuseum.modules.astro.mapper.CatalogVersionMapper;
import com.starmuseum.modules.astro.service.CatalogVersionService;
import com.starmuseum.modules.catalog.service.CatalogVersionAdminService;
import com.starmuseum.modules.catalog.vo.CatalogVersionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogVersionAdminServiceImpl implements CatalogVersionAdminService {

    private final CatalogVersionService catalogVersionService;
    private final CatalogVersionMapper catalogVersionMapper;

    @Override
    public CatalogVersionVO getActive() {
        CatalogVersion active = catalogVersionService.getActive();
        return active == null ? null : toVO(active);
    }

    @Override
    public List<CatalogVersionVO> listAll() {
        return catalogVersionService.listAll()
            .stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }

    @Override
    public CatalogVersionVO activate(String code) {
        CatalogVersion v = catalogVersionService.activate(code);
        return toVO(v);
    }

    @Override
    public CatalogVersionVO rollback(String targetCode) {
        if (StringUtils.hasText(targetCode)) {
            CatalogVersion v = catalogVersionService.activate(targetCode.trim());
            return toVO(v);
        }

        CatalogVersion current = catalogVersionService.getActive();
        if (current == null || !StringUtils.hasText(current.getCode())) {
            throw new BizException(400, "当前没有 active 版本，无法自动回滚");
        }

        String curCode = current.getCode();

        CatalogVersion prev = catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .isNotNull(CatalogVersion::getActivatedAt)
                .ne(CatalogVersion::getCode, curCode)
                .orderByDesc(CatalogVersion::getActivatedAt)
                .orderByDesc(CatalogVersion::getId)
                .last("limit 1")
        );

        if (prev == null) {
            throw new BizException(400, "没有上一激活版本可回滚");
        }

        CatalogVersion v = catalogVersionService.activate(prev.getCode());
        return toVO(v);
    }

    private CatalogVersionVO toVO(CatalogVersion v) {
        CatalogVersionVO vo = new CatalogVersionVO();
        vo.setCode(v.getCode());
        vo.setSchemaVersion(v.getSchemaVersion());
        vo.setStatus(v.getStatus());
        vo.setManifestChecksum(v.getManifestChecksum());
        vo.setBuildTime(v.getBuildTime());
        vo.setImportedAt(v.getImportedAt());
        vo.setValidatedAt(v.getValidatedAt());
        vo.setActivatedAt(v.getActivatedAt());
        vo.setLastError(v.getLastError());
        return vo;
    }
}
