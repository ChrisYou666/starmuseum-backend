-- 把 constellation_line 的字段改成项目全局规范：is_deleted / created_at / updated_at

ALTER TABLE constellation_line
    CHANGE COLUMN deleted is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常,1=删除',
    CHANGE COLUMN create_time created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHANGE COLUMN update_time updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
