package com.starmuseum.modules.catalog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.astro.entity.CatalogVersion;
import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.mapper.CatalogVersionMapper;
import com.starmuseum.modules.astro.mapper.CelestialBodyMapper;
import com.starmuseum.modules.catalog.dto.CatalogManifestDTO;
import com.starmuseum.modules.catalog.dto.CatalogValidateResponse;
import com.starmuseum.modules.catalog.mapper.CatalogValidationMapper;
import com.starmuseum.modules.catalog.mapper.DuplicateAliasRow;
import com.starmuseum.modules.catalog.service.CatalogValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4.1 Catalog Validation（MVP：objects）
 */
@Service
@RequiredArgsConstructor
public class CatalogValidationServiceImpl implements CatalogValidationService {

    private final CatalogVersionMapper catalogVersionMapper;
    private final CelestialBodyMapper bodyMapper;
    private final CatalogValidationMapper validationMapper;

    @Transactional
    @Override
    public CatalogValidateResponse validate(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(400, "code 不能为空");
        }
        code = code.trim();

        CatalogVersion ver = catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getCode, code)
                .last("limit 1")
        );
        if (ver == null) {
            throw new BizException(404, "catalog_version 不存在: " + code);
        }

        List<String> issues = new ArrayList<>();

        // 1) counts（如果 manifest.counts.objects 有值）
        Integer manifestCount = extractObjectsCountFromManifest(ver.getManifestJson());
        if (manifestCount != null && manifestCount >= 0) {
            long dbCount = bodyMapper.selectCount(
                new LambdaQueryWrapper<CelestialBody>()
                    .eq(CelestialBody::getCatalogVersionCode, code)
            );
            if (dbCount != manifestCount.longValue()) {
                issues.add("objects 数量不一致：manifest=" + manifestCount + " db=" + dbCount);
            }
        }

        // 2) 关键字段范围校验（抽样：发现即报）
        List<CelestialBody> invalids = bodyMapper.selectList(
            new LambdaQueryWrapper<CelestialBody>()
                .eq(CelestialBody::getCatalogVersionCode, code)
                .and(w -> w
                    .isNull(CelestialBody::getCatalogCode)
                    .or().isNull(CelestialBody::getName)
                    .or().lt(CelestialBody::getRaDeg, 0)
                    .or().ge(CelestialBody::getRaDeg, 360)
                    .or().lt(CelestialBody::getDecDeg, -90)
                    .or().gt(CelestialBody::getDecDeg, 90)
                )
                .last("limit 20")
        );
        for (CelestialBody b : invalids) {
            issues.add("对象字段不合法: id=" + b.getId() + ", catalogCode=" + b.getCatalogCode());
        }

        // 3) 别名冲突（同版本内：lang+aliasName 多个 body）
        List<DuplicateAliasRow> dups = validationMapper.findDuplicateAliases(code);
        for (DuplicateAliasRow r : dups) {
            issues.add("别名冲突: lang=" + r.getLang() + ", alias=" + r.getAliasName() + ", cnt=" + r.getCnt());
        }

        CatalogValidateResponse resp = new CatalogValidateResponse();
        resp.setCode(code);

        LocalDateTime now = LocalDateTime.now();

        if (issues.isEmpty()) {
            CatalogVersion upd = new CatalogVersion();
            upd.setId(ver.getId());
            upd.setStatus("VALIDATED");
            upd.setValidatedAt(now);
            upd.setLastError(null);
            upd.setUpdatedAt(now);
            catalogVersionMapper.updateById(upd);

            resp.setStatus("VALIDATED");
            resp.setValidatedAt(now);
            return resp;
        }

        CatalogVersion upd = new CatalogVersion();
        upd.setId(ver.getId());
        upd.setStatus("FAILED");
        upd.setLastError(String.join("; ", issues));
        upd.setUpdatedAt(now);
        catalogVersionMapper.updateById(upd);

        resp.setStatus("FAILED");
        resp.getIssues().addAll(issues);
        return resp;
    }

    private Integer extractObjectsCountFromManifest(String manifestJson) {
        if (!StringUtils.hasText(manifestJson)) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            CatalogManifestDTO m = om.readValue(manifestJson, CatalogManifestDTO.class);
            if (m.getCounts() == null) return null;
            return m.getCounts().get("objects");
        } catch (Exception e) {
            return null;
        }
    }
}
