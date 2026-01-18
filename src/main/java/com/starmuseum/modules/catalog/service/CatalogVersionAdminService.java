package com.starmuseum.modules.catalog.service;

import com.starmuseum.modules.catalog.vo.CatalogVersionVO;

import java.util.List;

public interface CatalogVersionAdminService {

    CatalogVersionVO getActive();

    List<CatalogVersionVO> listAll();

    CatalogVersionVO activate(String code);

    CatalogVersionVO rollback(String targetCode);
}
