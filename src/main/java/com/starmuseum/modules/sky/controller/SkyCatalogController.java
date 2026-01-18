package com.starmuseum.modules.sky.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注意：
 * 1) /api/sky/catalog/active 已被 CatalogPublicController 占用
 * 2) /api/sky/objects 已被 SkyObjectsController 占用
 *
 * 所以这个 Controller 暂时不提供接口，避免 Ambiguous mapping 导致启动失败。
 *
 * 后续如果你要扩展 sky 相关接口，可以在这里加“不会冲突”的新路径，
 * 例如：/api/sky/catalog/version 或 /api/sky/catalog/status 等。
 */
@RestController
@RequestMapping("/api/sky")
public class SkyCatalogController {
    // 暂空即可
}
