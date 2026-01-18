package com.starmuseum.modules.catalog.mapper;

import lombok.Data;

@Data
public class DuplicateAliasRow {
    private String lang;
    private String aliasName;
    private Long cnt;
}
