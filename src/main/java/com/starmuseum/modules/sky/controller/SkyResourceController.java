package com.starmuseum.modules.sky.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.modules.sky.service.TextureMetadataService;
import com.starmuseum.modules.sky.vo.TextureMetadataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sky")
public class SkyResourceController {

    private final TextureMetadataService textureMetadataService;

    /**
     * 贴图分档元数据
     * GET /api/sky/textures/metadata?key=milkyway
     */
    @GetMapping("/textures/metadata")
    public Result<TextureMetadataResponse> textureMetadata(@RequestParam("key") String key) {
        TextureMetadataResponse resp = textureMetadataService.getTextureMetadata(key);
        return Result.ok(resp);
    }
}
