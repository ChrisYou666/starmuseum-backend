package com.starmuseum.modules.astro.service;

import com.starmuseum.modules.astro.entity.CatalogVersion;

import java.util.List;

public interface CatalogVersionService {

    /**
     * 获取当前生效的 catalog 版本 code（status=ACTIVE）
     */
    String getActiveCatalogVersionCode();

    /**
     * 查询当前 ACTIVE 的版本（不存在返回 null）
     */
    CatalogVersion getActive();

    /**
     * 列出全部版本（按 activatedAt/createdAt 倒序）
     */
    List<CatalogVersion> listAll();

    /**
     * 激活指定版本（会把其他 ACTIVE 版本置为 INACTIVE）
     * @param code catalog 版本 code
     * @return 激活后的版本记录
     */
    CatalogVersion activate(String code);
}
