CREATE TABLE IF NOT EXISTS constellation_line (
                                                  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                  constellation_code  VARCHAR(32)  NOT NULL COMMENT 'IAU abbreviation/code, e.g. Ori',
    constellation_name  VARCHAR(64)  NOT NULL COMMENT 'Display name, e.g. Orion',
    start_body_id       BIGINT       NOT NULL COMMENT 'CelestialBody.id',
    end_body_id         BIGINT       NOT NULL COMMENT 'CelestialBody.id',
    sort_order          INT          NOT NULL DEFAULT 0 COMMENT 'Render order',
    remark              VARCHAR(255) NULL,
    deleted             TINYINT      NOT NULL DEFAULT 0 COMMENT '0=normal,1=deleted',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_constellation_code (constellation_code),
    INDEX idx_start_body (start_body_id),
    INDEX idx_end_body (end_body_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Constellation line segments';