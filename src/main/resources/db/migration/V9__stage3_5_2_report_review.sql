CREATE TABLE report_review (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             report_id BIGINT NOT NULL,
                             reviewer_user_id BIGINT NOT NULL,
                             decision VARCHAR(16) NULL COMMENT 'REJECT / RESOLVE（start 阶段可为空）',
                             notes VARCHAR(500) NULL,
                             created_at DATETIME NOT NULL,
                             UNIQUE KEY uk_report_review_report_id (report_id),
                             KEY idx_report_review_reviewer (reviewer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
