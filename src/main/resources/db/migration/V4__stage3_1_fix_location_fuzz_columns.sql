-- V4：确保 post 的模糊列为 *_fuzzy（幂等）
-- 若列已是 *_fuzzy，则什么都不做

-- LAT
SET @sql_lat := (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'post'
        AND COLUMN_NAME = 'location_lat_fuzz'
    ) THEN
      'ALTER TABLE post CHANGE COLUMN location_lat_fuzz location_lat_fuzzy DECIMAL(10,6) NULL'
    WHEN EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'post'
        AND COLUMN_NAME = 'location_lat_fuzzy'
    ) THEN
      'SELECT 1'
    ELSE
      'SELECT 1'
  END
);
PREPARE stmt_lat FROM @sql_lat;
EXECUTE stmt_lat;
DEALLOCATE PREPARE stmt_lat;

-- LON
SET @sql_lon := (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'post'
        AND COLUMN_NAME = 'location_lon_fuz'
    ) THEN
      'ALTER TABLE post CHANGE COLUMN location_lon_fuz location_lon_fuzzy DECIMAL(10,6) NULL'
    WHEN EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'post'
        AND COLUMN_NAME = 'location_lon_fuzz'
    ) THEN
      'ALTER TABLE post CHANGE COLUMN location_lon_fuzz location_lon_fuzzy DECIMAL(10,6) NULL'
    WHEN EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'post'
        AND COLUMN_NAME = 'location_lon_fuzzy'
    ) THEN
      'SELECT 1'
    ELSE
      'SELECT 1'
  END
);
PREPARE stmt_lon FROM @sql_lon;
EXECUTE stmt_lon;
DEALLOCATE PREPARE stmt_lon;
