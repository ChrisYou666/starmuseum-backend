-- Phase 4.1 Catalog Versioning
-- 目标：catalog 数据可导入/可校验/可激活/可回滚；线上查询只读 active 版本
-- 注意：你当前已 baseline-version=1，且 V2~V11 为阶段3迁移；因此阶段4从 V12 开始。

-- 1) catalog_version：增强字段（保留你原有字段 code/status/checksum/source_note/activated_at/created_at/updated_at）
ALTER TABLE `catalog_version`
  ADD COLUMN `schema_version` varchar(32) NULL DEFAULT NULL AFTER `code`,
  ADD COLUMN `manifest_json` json NULL AFTER `source_note`,
  ADD COLUMN `manifest_checksum` varchar(64) NULL DEFAULT NULL AFTER `manifest_json`,
  ADD COLUMN `build_time` datetime NULL DEFAULT NULL AFTER `manifest_checksum`,
  ADD COLUMN `imported_by` bigint NULL DEFAULT NULL AFTER `build_time`,
  ADD COLUMN `imported_at` datetime NULL DEFAULT NULL AFTER `imported_by`,
  ADD COLUMN `validated_at` datetime NULL DEFAULT NULL AFTER `imported_at`,
  ADD COLUMN `last_error` varchar(1024) NULL DEFAULT NULL AFTER `validated_at`;

-- 2) sys_kv_config：active 指针（推荐方案B：切换快、回滚简单、无多 ACTIVE 锁风险）
-- 若 key 不存在则插入；值默认取 catalog_version.status='ACTIVE' 的 code（如果存在）
INSERT INTO `sys_kv_config` (`config_key`, `config_value`, `remark`)
SELECT
  'active_catalog_version',
  (
    SELECT `code`
    FROM `catalog_version`
    WHERE `status` = 'ACTIVE'
    ORDER BY `activated_at` DESC, `id` DESC
    LIMIT 1
  ),
  'current active catalog version'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_kv_config` WHERE `config_key` = 'active_catalog_version'
  );

-- 3) celestial_body：补充常用联合索引（查询默认加 catalog_version_code）
-- 你的表里已经有 catalog_version_code，所以只需要建索引即可
CREATE INDEX `idx_body_ver_type_mag` ON `celestial_body` (`catalog_version_code`, `body_type`, `mag`);
CREATE INDEX `idx_body_ver_name` ON `celestial_body` (`catalog_version_code`, `name`);

-- 4) catalog_version：常用索引
CREATE INDEX `idx_catalog_status_activated` ON `catalog_version` (`status`, `activated_at`);
