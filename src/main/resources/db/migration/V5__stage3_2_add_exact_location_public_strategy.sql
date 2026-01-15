-- stage3.2 补丁：补齐 exact_location_public_strategy（兼容 MySQL 8.0 任意版本）
SET @exists := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'user_privacy_setting'
    AND COLUMN_NAME = 'exact_location_public_strategy'
);

SET @sql := IF(
  @exists = 0,
  'ALTER TABLE user_privacy_setting ADD COLUMN exact_location_public_strategy VARCHAR(16) NOT NULL DEFAULT ''FUZZY''',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
