-- V1: 基础表结构
-- 数据库：MySQL 8.x，字符集 utf8mb4

CREATE TABLE IF NOT EXISTS `location` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                          `name` VARCHAR(128) NOT NULL COMMENT '地点名称（唯一）',
    `country` VARCHAR(64) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省/州',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `timezone` VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '时区（IANA）',
    `altitude_m` INT DEFAULT NULL COMMENT '海拔(米)',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=未删,1=已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_location_name` (`name`),
    KEY `idx_location_city` (`city`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;