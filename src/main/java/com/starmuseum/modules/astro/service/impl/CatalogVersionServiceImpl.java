package com.starmuseum.modules.astro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.common.kv.service.SysKvConfigService;
import com.starmuseum.modules.astro.entity.CatalogVersion;
import com.starmuseum.modules.astro.mapper.CatalogVersionMapper;
import com.starmuseum.modules.astro.service.CatalogVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogVersionServiceImpl implements CatalogVersionService {

    public static final String KV_ACTIVE_CATALOG_VERSION = "active_catalog_version";

    private final CatalogVersionMapper catalogVersionMapper;
    private final SysKvConfigService kv;

    @Override
    public String getActiveCatalogVersionCode() {
        // 优先走 KV 指针（阶段4.1 推荐方案B）
        String code = kv.get(KV_ACTIVE_CATALOG_VERSION);
        if (StringUtils.hasText(code)) {
            // 兜底：如果 KV 指向的版本在表里不存在（异常数据），则继续走旧逻辑
            CatalogVersion exists = catalogVersionMapper.selectOne(
                new LambdaQueryWrapper<CatalogVersion>()
                    .eq(CatalogVersion::getCode, code)
                    .last("limit 1")
            );
            if (exists != null) {
                return code;
            }
        }

        // 兼容旧逻辑：status = ACTIVE
        CatalogVersion active = getActive();
        return active == null ? null : active.getCode();
    }

    @Override
    public CatalogVersion getActive() {
        // 1) KV 指针优先
        String code = kv.get(KV_ACTIVE_CATALOG_VERSION);
        if (StringUtils.hasText(code)) {
            CatalogVersion row = catalogVersionMapper.selectOne(
                new LambdaQueryWrapper<CatalogVersion>()
                    .eq(CatalogVersion::getCode, code)
                    .last("limit 1")
            );
            if (row != null) {
                return row;
            }
        }

        // 2) 兼容旧逻辑：status = ACTIVE
        return catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getStatus, "ACTIVE")
                .orderByDesc(CatalogVersion::getActivatedAt)
                .orderByDesc(CatalogVersion::getId)
                .last("limit 1")
        );
    }

    @Override
    public List<CatalogVersion> listAll() {
        // activatedAt 可能为空（未激活过），所以再用 createdAt 兜底排序
        return catalogVersionMapper.selectList(
            new LambdaQueryWrapper<CatalogVersion>()
                .orderByDesc(CatalogVersion::getActivatedAt)
                .orderByDesc(CatalogVersion::getCreatedAt)
                .orderByDesc(CatalogVersion::getId)
        );
    }

    /**
     * 兼容旧接口：/api/astro/catalog/version/activate
     *
     * 阶段4.1 语义：
     * - 只有 1 个 active（以 sys_kv_config.active_catalog_version 为准）
     * - 为了兼容历史代码/调试，也会同步更新 catalog_version.status
     */
    @Transactional
    @Override
    public CatalogVersion activate(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(400, "code 不能为空");
        }

        CatalogVersion target = catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getCode, code)
                .last("limit 1")
        );

        if (target == null) {
            throw new BizException(404, "catalog_version 不存在: " + code);
        }

        // 只允许已校验（VALIDATED）或已激活过（ACTIVE/INACTIVE）的版本被激活
        String st = target.getStatus();
        if (StringUtils.hasText(st) && !(st.equals("VALIDATED") || st.equals("ACTIVE") || st.equals("INACTIVE"))) {
            throw new BizException(400, "当前版本状态不允许激活: " + st + "（需要 VALIDATED/ACTIVE/INACTIVE）");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1) 读取旧 active
        String oldActiveCode = getActiveCatalogVersionCode();
        if (StringUtils.hasText(oldActiveCode) && !oldActiveCode.equals(code)) {
            CatalogVersion old = catalogVersionMapper.selectOne(
                new LambdaQueryWrapper<CatalogVersion>()
                    .eq(CatalogVersion::getCode, oldActiveCode)
                    .last("limit 1")
            );
            if (old != null) {
                CatalogVersion updOld = new CatalogVersion();
                updOld.setId(old.getId());
                updOld.setStatus("INACTIVE");
                updOld.setUpdatedAt(now);
                catalogVersionMapper.updateById(updOld);
            }
        }

        // 2) 写 KV active 指针（source of truth）
        kv.set(KV_ACTIVE_CATALOG_VERSION, code, "current active catalog version");

        // 3) 同步更新目标状态为 ACTIVE（兼容旧逻辑）
        CatalogVersion upd = new CatalogVersion();
        upd.setId(target.getId());
        upd.setStatus("ACTIVE");
        upd.setActivatedAt(now);
        upd.setUpdatedAt(now);
        catalogVersionMapper.updateById(upd);

        return catalogVersionMapper.selectById(target.getId());
    }
}
