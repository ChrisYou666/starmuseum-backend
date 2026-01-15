-- =========================================
-- V2 - Stage 3.1 - Post 位置隐私字段
-- 说明：
-- 1) location_lat/location_lon：精确坐标（仅作者可见）
-- 2) location_lat_fuzzy/location_lon_fuzzy：模糊坐标（对他人展示）
-- 3) location_visibility：HIDDEN/CITY/FUZZY/EXACT
-- 4) location_city：阶段3先由前端传 cityName（阶段4再做反查/逆地理编码）
-- =========================================

ALTER TABLE `post`
  ADD COLUMN `location_visibility` VARCHAR(16) NULL COMMENT '位置可见性：HIDDEN/CITY/FUZZY/EXACT' AFTER `visibility`,
    ADD COLUMN `location_lat` DECIMAL(10,6) NULL COMMENT '精确纬度（仅作者可见）' AFTER `location_visibility`,
    ADD COLUMN `location_lon` DECIMAL(10,6) NULL COMMENT '精确经度（仅作者可见）' AFTER `location_lat`,
    ADD COLUMN `location_city` VARCHAR(64) NULL COMMENT '城市名（阶段3前端传）' AFTER `location_lon`,
    ADD COLUMN `location_lat_fuzzy` DECIMAL(10,6) NULL COMMENT '模糊纬度（对他人展示）' AFTER `location_city`,
    ADD COLUMN `location_lon_fuzzy` DECIMAL(10,6) NULL COMMENT '模糊经度（对他人展示）' AFTER `location_lat_fuzzy`;

CREATE INDEX `idx_post_location_visibility` ON `post` (`location_visibility`);
