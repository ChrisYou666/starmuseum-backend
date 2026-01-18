-- src/main/resources/db/migration/V13__phase5_5a_device_profile.sql
-- Phase 5A Device Profile + FOV
-- 目标：支持摄影（PHOTO）与目视（VISUAL）两种设备配置，供 FOV 计算与后续观测日志引用。

CREATE TABLE astro_device_profile (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT NOT NULL,
                                    name VARCHAR(64) NOT NULL,
                                    type VARCHAR(16) NOT NULL, -- PHOTO / VISUAL

  -- PHOTO 参数：相机/镜头（单位：mm）
                                    sensor_width_mm DOUBLE NULL,
                                    sensor_height_mm DOUBLE NULL,
                                    focal_length_mm DOUBLE NULL,

  -- VISUAL 参数：望远镜/目镜（单位：mm；AFOV 单位：deg）
                                    telescope_focal_mm DOUBLE NULL,
                                    eyepiece_focal_mm DOUBLE NULL,
                                    eyepiece_afov_deg DOUBLE NULL,

  -- 默认标记：同一 user_id 在同一 type 下只能有一个 default（由业务层保证）
                                    is_default TINYINT NOT NULL DEFAULT 0,

                                    created_at DATETIME NOT NULL,
                                    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 索引（阶段5A建议）
CREATE INDEX idx_device_profile_user_default ON astro_device_profile(user_id, is_default);
CREATE INDEX idx_device_profile_user_type ON astro_device_profile(user_id, type);
CREATE INDEX idx_device_profile_user_name ON astro_device_profile(user_id, name);
