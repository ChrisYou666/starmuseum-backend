CREATE TABLE IF NOT EXISTS `tb_celestial_body`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name`          VARCHAR(128) NOT NULL COMMENT '名称（如 Sirius、Mars）',
    `type`          VARCHAR(32)  NOT NULL COMMENT '类型（STAR/PLANET/MOON/...)',
    `constellation` VARCHAR(64)  NULL COMMENT '所属星座（可空）',
    `alias`         VARCHAR(128) NULL COMMENT '别名/中文名（可空）',

    `ra_hours`      DOUBLE NULL COMMENT '赤经（小时制，0~24）',
    `dec_degrees`   DOUBLE NULL COMMENT '赤纬（度，-90~90）',
    `magnitude`     DOUBLE NULL COMMENT '视星等（越小越亮）',
    `distance_ly`   DOUBLE NULL COMMENT '距离（光年）',

    `spectral_type` VARCHAR(32)  NULL COMMENT '光谱型（如 G2V）',
    `temperature_k` INT          NULL COMMENT '表面温度（K）',
    `description`   TEXT         NULL COMMENT '描述',

    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT  NOT NULL DEFAULT 0,

    INDEX `idx_celestial_name` (`name`),
    INDEX `idx_celestial_type` (`type`),
    INDEX `idx_celestial_mag` (`magnitude`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;