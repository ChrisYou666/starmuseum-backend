CREATE TABLE IF NOT EXISTS user_block (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        user_id BIGINT NOT NULL,
                                        blocked_user_id BIGINT NOT NULL,
                                        created_at DATETIME NOT NULL,
                                        UNIQUE KEY uk_user_block (user_id, blocked_user_id),
  KEY idx_user_block_user_id (user_id),
  KEY idx_user_block_blocked_user_id (blocked_user_id)
  );
