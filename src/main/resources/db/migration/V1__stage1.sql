/* =========================================================
   StarMuseum - Stage1 Schema (IAM + Social + Media)
   MySQL 8+
   ========================================================= */

-- 可选：如果你用 docker-entrypoint-initdb.d/scripts/init.sql 执行，建议保留
CREATE DATABASE IF NOT EXISTS `starmuseum`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `starmuseum`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 1) user
-- =========================================================
CREATE TABLE IF NOT EXISTS `user` (
                                    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                    `email`           VARCHAR(128) NULL,
  `password_hash`   VARCHAR(128) NOT NULL,
  `nickname`        VARCHAR(64)  NOT NULL,
  `avatar_url`      VARCHAR(512) NULL,
  `bio`             VARCHAR(512) NULL,
  `status`          VARCHAR(16)  NOT NULL DEFAULT 'NORMAL', -- NORMAL/BANNED(预留)
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 2) user_privacy_setting
-- =========================================================
CREATE TABLE IF NOT EXISTS `user_privacy_setting` (
                                                    `user_id`                 BIGINT UNSIGNED NOT NULL,
                                                    `post_visibility_default` VARCHAR(16)     NOT NULL DEFAULT 'PUBLIC', -- PUBLIC/PRIVATE/FOLLOWERS(预留)
  `created_at`              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_privacy_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 3) user_session (refresh token 存储与撤销)
-- =========================================================
CREATE TABLE IF NOT EXISTS `user_session` (
                                            `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                            `user_id`            BIGINT UNSIGNED NOT NULL,
                                            `refresh_token_hash` VARCHAR(256)    NOT NULL,           -- 不存明文
  `expires_at`         DATETIME        NOT NULL,
  `revoked_at`         DATETIME        NULL,
  `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_used_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_user` (`user_id`),
  KEY `idx_session_expires` (`expires_at`),
  KEY `idx_session_revoked` (`revoked_at`),
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 4) media (图片上传 + 缩略图)
-- =========================================================
CREATE TABLE IF NOT EXISTS `media` (
                                     `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                     `user_id`       BIGINT UNSIGNED NOT NULL,                -- 上传者/归属者
                                     `mime_type`     VARCHAR(64)     NOT NULL,                -- image/jpeg, image/png, image/webp
  `size_bytes`    BIGINT UNSIGNED NOT NULL,
  `width`         INT            NULL,
  `height`        INT            NULL,
  `sha256`        CHAR(64)        NULL,                    -- 可选：用于去重/校验
  `storage_type`  VARCHAR(16)     NOT NULL DEFAULT 'LOCAL', -- LOCAL/MINIO(预留)
  `storage_key`   VARCHAR(512)    NOT NULL,                -- 本地相对路径或 minio object key
  `url_original`  VARCHAR(512)    NOT NULL,
  `url_medium`    VARCHAR(512)    NULL,
  `url_thumb`     VARCHAR(512)    NULL,
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_media_user_time` (`user_id`, `created_at`),
  KEY `idx_media_sha256` (`sha256`),
  CONSTRAINT `fk_media_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 5) post (动态)
-- =========================================================
CREATE TABLE IF NOT EXISTS `post` (
                                    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                    `user_id`      BIGINT UNSIGNED NOT NULL,
                                    `content`      TEXT            NULL,
                                    `visibility`   VARCHAR(16)     NOT NULL DEFAULT 'PUBLIC', -- PUBLIC/PRIVATE/FOLLOWERS(预留)
  `like_count`   INT UNSIGNED    NOT NULL DEFAULT 0,
  `comment_count`INT UNSIGNED    NOT NULL DEFAULT 0,
  `deleted_at`   DATETIME        NULL,
  `deleted_by`   BIGINT UNSIGNED NULL,
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_user_time` (`user_id`, `created_at`),
  KEY `idx_post_visibility_time` (`visibility`, `created_at`),
  KEY `idx_post_deleted_at` (`deleted_at`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 6) post_media (动态-图片关联，最多9张由业务层控制)
-- =========================================================
CREATE TABLE IF NOT EXISTS `post_media` (
                                          `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                          `post_id`    BIGINT UNSIGNED NOT NULL,
                                          `media_id`   BIGINT UNSIGNED NOT NULL,
                                          `sort_no`    INT UNSIGNED    NOT NULL DEFAULT 0,
                                          `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_media_sort` (`post_id`, `sort_no`),
  KEY `idx_post_media_post` (`post_id`),
  KEY `idx_post_media_media` (`media_id`),
  CONSTRAINT `fk_post_media_post` FOREIGN KEY (`post_id`) REFERENCES `post`(`id`),
  CONSTRAINT `fk_post_media_media` FOREIGN KEY (`media_id`) REFERENCES `media`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 7) post_like (点赞，幂等核心：唯一约束 post_id+user_id)
-- =========================================================
CREATE TABLE IF NOT EXISTS `post_like` (
                                         `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                         `post_id`    BIGINT UNSIGNED NOT NULL,
                                         `user_id`    BIGINT UNSIGNED NOT NULL,
                                         `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like_post_user` (`post_id`, `user_id`),    -- 幂等兜底
  KEY `idx_like_post` (`post_id`),
  KEY `idx_like_user` (`user_id`),
  CONSTRAINT `fk_like_post` FOREIGN KEY (`post_id`) REFERENCES `post`(`id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 8) comment (评论 + 软删)
-- =========================================================
CREATE TABLE IF NOT EXISTS `comment` (
                                       `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                       `post_id`    BIGINT UNSIGNED NOT NULL,
                                       `user_id`    BIGINT UNSIGNED NOT NULL,
                                       `content`    TEXT            NOT NULL,
                                       `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `deleted_at` DATETIME        NULL,
                                       `deleted_by` BIGINT UNSIGNED NULL,
                                       PRIMARY KEY (`id`),
  KEY `idx_comment_post_time` (`post_id`, `created_at`),
  KEY `idx_comment_user_time` (`user_id`, `created_at`),
  KEY `idx_comment_deleted_at` (`deleted_at`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post`(`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
