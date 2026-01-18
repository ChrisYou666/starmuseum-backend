package com.starmuseum.modules.catalog.service;

import com.starmuseum.modules.catalog.dto.CatalogImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CatalogImportService {
    CatalogImportResponse importZip(MultipartFile file);
}
