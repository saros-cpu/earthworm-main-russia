-- 001_init.sql - Earthworm MySQL Schema
-- Manually generated from packages/schema/src/schema/*.ts

DROP TABLE IF EXISTS `user_learn_record`;
DROP TABLE IF EXISTS `user_learning_activities`;
DROP TABLE IF EXISTS `mastered_elements`;
DROP TABLE IF EXISTS `memberships`;
DROP TABLE IF EXISTS `user_course_progress`;
DROP TABLE IF EXISTS `course_history`;
DROP TABLE IF EXISTS `statements`;
DROP TABLE IF EXISTS `courses`;
DROP TABLE IF EXISTS `course_packs`;

-- course_packs
CREATE TABLE `course_packs` (
  `id` VARCHAR(128) NOT NULL,
  `order` INT NOT NULL,
  `title` TEXT NOT NULL,
  `description` TEXT,
  `is_free` TINYINT(1),
  `cover` TEXT,
  `creator_id` TEXT NOT NULL,
  `share_level` VARCHAR(64) DEFAULT 'private',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- courses
CREATE TABLE `courses` (
  `id` VARCHAR(128) NOT NULL,
  `title` TEXT NOT NULL,
  `description` TEXT,
  `video` TEXT,
  `order` INT NOT NULL,
  `course_pack_id` VARCHAR(128) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_courses_course_pack_id` (`course_pack_id`),
  CONSTRAINT `fk_courses_course_pack_id`
    FOREIGN KEY (`course_pack_id`) REFERENCES `course_packs` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- statements
CREATE TABLE `statements` (
  `id` VARCHAR(128) NOT NULL,
  `order` INT NOT NULL,
  `chinese` TEXT NOT NULL,
  `english` TEXT NOT NULL,
  `soundmark` TEXT NOT NULL,
  `course_id` VARCHAR(128) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_statements_course_id` (`course_id`),
  CONSTRAINT `fk_statements_course_id`
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- course_history
CREATE TABLE `course_history` (
  `id` VARCHAR(128) NOT NULL,
  `user_id` VARCHAR(128) NOT NULL,
  `course_id` VARCHAR(128) NOT NULL,
  `course_pack_id` VARCHAR(128) NOT NULL,
  `completion_count` INT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_history_user_course_pack`
    (`user_id`, `course_id`, `course_pack_id`),
  KEY `idx_course_history_course_id` (`course_id`),
  KEY `idx_course_history_course_pack_id` (`course_pack_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_course_progress
CREATE TABLE `user_course_progress` (
  `id` VARCHAR(128) NOT NULL,
  `user_id` VARCHAR(128) NOT NULL,
  `course_pack_id` VARCHAR(128) NOT NULL,
  `course_id` VARCHAR(128) NOT NULL,
  `statement_index` INT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course_progress_user_pack`
    (`user_id`, `course_pack_id`),
  KEY `idx_user_course_progress_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- memberships
CREATE TABLE `memberships` (
  `id` VARCHAR(128) NOT NULL,
  `user_id` VARCHAR(128) NOT NULL,
  `start_date` TIMESTAMP NOT NULL,
  `end_date` TIMESTAMP NOT NULL,
  `isActive` TINYINT(1) DEFAULT 1,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `type` VARCHAR(64) NOT NULL DEFAULT 'regular',
  PRIMARY KEY (`id`),
  KEY `idx_memberships_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- mastered_elements
CREATE TABLE `mastered_elements` (
  `id` VARCHAR(128) NOT NULL,
  `user_id` VARCHAR(128) NOT NULL,
  `content` JSON NOT NULL,
  `mastered_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_mastered_elements_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_learning_activities
CREATE TABLE `user_learning_activities` (
  `id` VARCHAR(128) NOT NULL,
  `user_id` VARCHAR(128) NOT NULL,
  `date` DATE NOT NULL,
  `activity_type` VARCHAR(64) NOT NULL,
  `course_id` VARCHAR(128),
  `duration` INT NOT NULL,
  `metadata` JSON,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_learning_activities`
    (`user_id`, `date`, `activity_type`(64)),
  KEY `idx_user_learning_activities_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_learn_record
CREATE TABLE `user_learn_record` (
  `id` VARCHAR(128) NOT NULL,
  `user_id` VARCHAR(128) NOT NULL,
  `count` INT NOT NULL DEFAULT 0,
  `day` DATE NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_learn_record` (`user_id`, `day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;