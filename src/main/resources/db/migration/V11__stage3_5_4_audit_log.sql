-- 创建 audit_log 表
CREATE TABLE audit_log (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         operator_user_id BIGINT NOT NULL,  -- 操作人 ID（管理员）
                         action VARCHAR(64) NOT NULL,  -- 操作类型（如：REPORT_START、REPORT_REVIEW、ACTION_MUTE）
                         entity_type VARCHAR(32) NOT NULL,  -- 操作对象类型（如：REPORT、POST、COMMENT、USER）
                         entity_id BIGINT NOT NULL,  -- 操作对象的 ID
                         detail_json TEXT NULL,  -- 请求体快照（JSON）
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
                         INDEX idx_operator_user_id (operator_user_id),
                         INDEX idx_entity_type_entity_id (entity_type, entity_id)
);
