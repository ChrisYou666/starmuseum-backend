package com.starmuseum.modules.sky.vo;

import lombok.Data;

import java.util.List;

@Data
public class TextureMetadataResponse {
    private String key;                 // 例如 milkyway
    private List<TextureVariantVO> variants;
}
