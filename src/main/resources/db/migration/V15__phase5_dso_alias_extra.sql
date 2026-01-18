-- Phase 5D: DSO 扩展 + alias 搜索增强（方案A：catalog 落 celestial_body/celestial_alias）
-- 目标：
-- 1) celestial_body 增加 extra_json，用于承载角直径/尺寸等扩展字段
-- 2) 增加必要索引：catalog_version_code + body_type + catalog_code；以及 celestial_alias 的 alias 搜索索引
-- 说明：
-- - MySQL 不支持 CREATE INDEX IF NOT EXISTS，因此用 information_schema.statistics 做幂等判断
-- - ADD COLUMN 同理，用 information_schema.columns 判断

-- ========== 1) celestial_body 增加 extra_json（幂等） ==========
SET @col_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'celestial_body'
    AND column_name = 'extra_json'
);

SET @sql := IF(@col_exists = 0,
  'ALTER TABLE celestial_body ADD COLUMN extra_json TEXT NULL AFTER wiki_url',
  'SELECT ''celestial_body.extra_json already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 2) idx_body_ver_type_code（幂等） ==========
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'celestial_body'
    AND index_name = 'idx_body_ver_type_code'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_body_ver_type_code ON celestial_body (catalog_version_code, body_type, catalog_code)',
  'SELECT ''idx_body_ver_type_code already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 3) idx_alias_name（幂等） ==========
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'celestial_alias'
    AND index_name = 'idx_alias_name'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_alias_name ON celestial_alias (alias_name)',
  'SELECT ''idx_alias_name already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 4) idx_alias_lang_name（幂等） ==========
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'celestial_alias'
    AND index_name = 'idx_alias_lang_name'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_alias_lang_name ON celestial_alias (lang, alias_name)',
  'SELECT ''idx_alias_lang_name already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 5) idx_alias_body_id（幂等） ==========
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'celestial_alias'
    AND index_name = 'idx_alias_body_id'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_alias_body_id ON celestial_alias (body_id)',
  'SELECT ''idx_alias_body_id already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
