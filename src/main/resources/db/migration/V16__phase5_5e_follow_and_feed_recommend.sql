-- 5E: Follow关系 + Feed 推荐（关注优先/热门补充）
-- 说明：MySQL 不支持 CREATE INDEX IF NOT EXISTS，因此用 information_schema 做幂等判断

-- ========== 1) user_follow 表 ==========
CREATE TABLE IF NOT EXISTS user_follow (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         follower_id BIGINT NOT NULL,
                                         followee_id BIGINT NOT NULL,
                                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         UNIQUE KEY uk_follower_followee (follower_id, followee_id),
  KEY idx_follower_created (follower_id, created_at),
  KEY idx_followee_created (followee_id, created_at)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== 2) post 表索引（幂等）==========
-- Feed 热门排序需要：visibility + deleted_at + created_at
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'post'
    AND index_name = 'idx_post_visibility_deleted_created'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_post_visibility_deleted_created ON post (visibility, deleted_at, created_at)',
  'SELECT ''idx_post_visibility_deleted_created already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
