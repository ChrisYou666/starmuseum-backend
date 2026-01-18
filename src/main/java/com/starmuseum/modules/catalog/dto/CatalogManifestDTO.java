package com.starmuseum.modules.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * manifest.json DTO（阶段4.1）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogManifestDTO {

    /**
     * 必填：catalogVersion（如 v2026_01）
     */
    @NotBlank
    private String catalogVersion;

    /**
     * 可选：buildTime（ISO-8601 字符串）
     */
    private String buildTime;

    /**
     * 可选：schemaVersion
     */
    private String schemaVersion;

    /**
     * 可选：dataTypes（objects/textures/exhibits）
     */
    private List<String> dataTypes;

    /**
     * 可选：counts（对象数量/贴图数量等）
     * 例如：{"objects": 1000, "textures": 12}
     */
    private Map<String, Integer> counts;
}
