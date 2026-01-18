package com.starmuseum.modules.sky.controller;

import com.starmuseum.modules.sky.service.TextureMetaService;
import com.starmuseum.modules.sky.vo.TextureMetaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sky/textures")
@RequiredArgsConstructor
public class SkyTextureController {

    private final TextureMetaService textureMetaService;

    /**
     * 贴图分辨率梯度元数据
     * 示例：
     * GET /api/sky/textures/meta?scene=milkyway
     */
    @GetMapping("/meta")
    public TextureMetaResponse meta(@RequestParam(required = false) String scene) {
        return textureMetaService.getTextureMeta(scene);
    }
}
