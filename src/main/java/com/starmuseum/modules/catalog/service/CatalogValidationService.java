package com.starmuseum.modules.catalog.service;

import com.starmuseum.modules.catalog.dto.CatalogValidateResponse;

public interface CatalogValidationService {
    CatalogValidateResponse validate(String code);
}
