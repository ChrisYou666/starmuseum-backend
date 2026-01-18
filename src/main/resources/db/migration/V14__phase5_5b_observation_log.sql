-- Phase 5B: Observation Log + Publish to Post (MVP)
-- 表：observation_log / observation_log_target / observation_log_media / observation_log_post_link
-- 说明：
-- 1) 位置字段遵循阶段3思路：location_visibility + exact lat/lon + fuzzy lat/lon + city
-- 2) 软删：deleted_at
-- 3) 设备引用：device_profile_id -> Phase5A astro_device_profile.id
-- 4) 方式：method (PHOTO / VISUAL / OTHER)

CREATE TABLE observation_log (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,

                               observed_at DATETIME NOT NULL,
                               method VARCHAR(16) NOT NULL, -- PHOTO / VISUAL / OTHER
                               device_profile_id BIGINT NULL,

                               notes VARCHAR(2000) NULL,

                               success TINYINT NULL, -- 1=成功 0=失败
                               rating INT NULL,      -- 1~5

  -- location governance (stage3 style)
                               location_visibility VARCHAR(16) NOT NULL DEFAULT 'HIDDEN', -- EXACT / FUZZY / HIDDEN
                               location_lat DOUBLE NULL,
                               location_lon DOUBLE NULL,
                               location_lat_fuzzy DOUBLE NULL,
                               location_lon_fuzzy DOUBLE NULL,
                               location_city VARCHAR(64) NULL,

  -- publish state (optional, actual link stored in link table)
                               published TINYINT NOT NULL DEFAULT 0,

                               created_at DATETIME NOT NULL,
                               updated_at DATETIME NOT NULL,
                               deleted_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE observation_log_target (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      log_id BIGINT NOT NULL,
                                      user_id BIGINT NOT NULL,

                                      target_type VARCHAR(32) NOT NULL, -- CELESTIAL_BODY / TEXT
                                      target_id BIGINT NULL,            -- 若 CELESTIAL_BODY 可填
                                      target_name VARCHAR(128) NOT NULL, -- 展示用名称（M42 / Andromeda / ...）
                                      body_type VARCHAR(32) NULL,        -- DSO/STAR/PLANET/...（可选）

                                      created_at DATETIME NOT NULL,
                                      updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE observation_log_media (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     log_id BIGINT NOT NULL,
                                     user_id BIGINT NOT NULL,

                                     media_id BIGINT NOT NULL, -- 引用 media.id（复用现有 media）
                                     sort_order INT NOT NULL DEFAULT 0,

                                     created_at DATETIME NOT NULL,
                                     updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 日志与帖子关联（可选，但文档要求“能回链”，所以这里实现）
CREATE TABLE observation_log_post_link (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         log_id BIGINT NOT NULL,
                                         user_id BIGINT NOT NULL,

                                         post_id BIGINT NOT NULL,

                                         created_at DATETIME NOT NULL,
                                         updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX idx_obs_log_user_time ON observation_log(user_id, observed_at);
CREATE INDEX idx_obs_log_user_published ON observation_log(user_id, published);
CREATE INDEX idx_obs_log_deleted ON observation_log(deleted_at);

CREATE INDEX idx_obs_target_log ON observation_log_target(log_id);
CREATE INDEX idx_obs_target_user ON observation_log_target(user_id, target_type);

CREATE INDEX idx_obs_media_log ON observation_log_media(log_id);
CREATE INDEX idx_obs_media_user ON observation_log_media(user_id, media_id);

CREATE UNIQUE INDEX uk_obs_log_post ON observation_log_post_link(log_id);
CREATE INDEX idx_obs_post_user ON observation_log_post_link(user_id, post_id);
