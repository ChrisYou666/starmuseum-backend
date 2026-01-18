package com.starmuseum.modules.catalog.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * objects.json / objects.jsonl 的单条对象结构（支持 DSO + alias + extra）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogObjectDTO {

    private String catalogCode;
    private String bodyType;

    private String name;
    private String nameZh;
    private String nameEn;
    private String nameId;

    private Double raDeg;
    private Double decDeg;

    private Double mag;

    private String spectralType;
    private String constellation;
    private String wikiUrl;

    /**
     * 可选：别名列表（用于搜索）
     * - aliasName 允许多语言
     */
    private List<AliasItem> aliases = new ArrayList<>();

    /**
     * Phase 5D：扩展字段收集容器
     * - objects.json 里除了已定义字段外的内容（如 angularDiameterArcmin/majorAxisArcmin/minorAxisArcmin/objClass 等）
     * - 会被 @JsonAnySetter 收集到这里，最终写入 celestial_body.extra_json
     */
    private Map<String, Object> extra = new HashMap<>();

    @JsonAnySetter
    public void putExtra(String key, Object value) {
        if (key == null) return;
        // 避免把 aliases 等重复塞进来（一般不会，因为它是已定义字段）
        extra.put(key, value);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AliasItem {
        private String lang;      // zh/en/id/any
        private String aliasName; // 别名
    }
}
