-- 3.5.1 举报（Report）+ 举报证据（Report Evidence）

CREATE TABLE report (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      reporter_user_id BIGINT NOT NULL,
                      target_type VARCHAR(16) NOT NULL,
                      target_id BIGINT NOT NULL,
                      reason_code VARCHAR(32) NOT NULL,
                      description VARCHAR(500) NULL,
                      status VARCHAR(16) NOT NULL,
                      created_at DATETIME NOT NULL,
                      updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_report_target ON report(target_type, target_id);
CREATE INDEX idx_report_reporter ON report(reporter_user_id);

CREATE TABLE report_evidence (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               report_id BIGINT NOT NULL,
                               media_id BIGINT NOT NULL,
                               created_at DATETIME NOT NULL,
                               UNIQUE KEY uk_report_evidence_report_media(report_id, media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_report_evidence_report_id ON report_evidence(report_id);
