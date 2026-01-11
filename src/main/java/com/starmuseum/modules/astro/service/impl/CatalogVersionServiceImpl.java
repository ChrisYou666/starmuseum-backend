package com.starmuseum.modules.astro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.astro.entity.CatalogVersion;
import com.starmuseum.modules.astro.mapper.CatalogVersionMapper;
import com.starmuseum.modules.astro.service.CatalogVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogVersionServiceImpl implements CatalogVersionService {

    private final CatalogVersionMapper catalogVersionMapper;

    @Override
    public String getActiveCatalogVersionCode() {
        CatalogVersion active = getActive();
        return active == null ? null : active.getCode();
    }

    @Override
    public CatalogVersion getActive() {
        return catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getStatus, "ACTIVE")
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
        );
    }

    @Transactional
    @Override
    public CatalogVersion activate(String code) {
        CatalogVersion target = catalogVersionMapper.selectOne(
            new LambdaQueryWrapper<CatalogVersion>()
                .eq(CatalogVersion::getCode, code)
                .last("limit 1")
        );

        if (target == null) {
            // 用你现有的 BizException，交给全局异常处理输出统一错误结构
            throw new BizException(404, "catalog_version 不存在: " + code);
        }

        LocalDateTime now = LocalDateTime.now();

        // 1) 把现有 ACTIVE 全部置为 INACTIVE（避免多 ACTIVE）
        CatalogVersion active = getActive();
        if (active != null && !active.getCode().equals(code)) {
            CatalogVersion toUpdate = new CatalogVersion();
            toUpdate.setId(active.getId());
            toUpdate.setStatus("INACTIVE");
            toUpdate.setUpdatedAt(now);
            catalogVersionMapper.updateById(toUpdate);
        }

        // 2) 激活目标版本
        CatalogVersion updateTarget = new CatalogVersion();
        updateTarget.setId(target.getId());
        updateTarget.setStatus("ACTIVE");
        updateTarget.setActivatedAt(now);
        updateTarget.setUpdatedAt(now);

        // createdAt 如果你没有自动填充，这里尽量不去覆盖（保持原值）
        catalogVersionMapper.updateById(updateTarget);

        // 3) 返回最新数据
        return catalogVersionMapper.selectById(target.getId());
    }
}
