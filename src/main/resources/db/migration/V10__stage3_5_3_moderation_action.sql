CREATE TABLE moderation_action (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 action_type VARCHAR(16) NOT NULL,
                                 target_user_id BIGINT,
                                 target_type VARCHAR(16),
                                 target_id BIGINT,
                                 duration_hours INT DEFAULT NULL,
                                 reason VARCHAR(500),
                                 operator_user_id BIGINT NOT NULL,
                                 related_report_id BIGINT DEFAULT NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 INDEX idx_action_target_user (target_user_id),
                                 INDEX idx_action_report (related_report_id)
);
ALTER TABLE user
  ADD COLUMN muted_until DATETIME NULL,
    ADD COLUMN suspended_until DATETIME NULL,
    ADD COLUMN banned TINYINT NOT NULL DEFAULT 0;
