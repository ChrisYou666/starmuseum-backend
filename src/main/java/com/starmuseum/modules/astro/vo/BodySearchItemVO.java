package com.starmuseum.modules.astro.vo;

import lombok.Data;

@Data
public class BodySearchItemVO {

    private Long id;
    private String catalogCode;
    private String bodyType;

    private String name;
    private String nameZh;
    private String nameEn;
    private String nameId;

    private Double mag;
}
