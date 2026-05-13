-- Exercise records: per-question history
CREATE TABLE exercise_records (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    course_pack_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    statement_id VARCHAR(128) NOT NULL,
    correct TINYINT(1) NOT NULL DEFAULT 0,
    attempts INT NOT NULL DEFAULT 1,
    time_spent_ms INT NOT NULL DEFAULT 0,
    score INT NOT NULL DEFAULT 0,
    combo_at_time INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_exercise_records_user (user_id),
    KEY idx_exercise_records_date (created_at),
    KEY idx_exercise_records_user_statement (user_id, statement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Daily aggregated learning stats
CREATE TABLE daily_stats (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    date DATE NOT NULL,
    total_exercises INT NOT NULL DEFAULT 0,
    correct_exercises INT NOT NULL DEFAULT 0,
    total_time_seconds INT NOT NULL DEFAULT 0,
    max_combo INT NOT NULL DEFAULT 0,
    total_score INT NOT NULL DEFAULT 0,
    courses_completed INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_stats_user_date (user_id, date),
    KEY idx_daily_stats_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Review schedule (SM-2 spaced repetition)
CREATE TABLE review_schedule (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    statement_id VARCHAR(128) NOT NULL,
    course_pack_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    easiness DOUBLE NOT NULL DEFAULT 2.50,
    `interval` INT NOT NULL DEFAULT 0,
    repetitions INT NOT NULL DEFAULT 0,
    next_review_at DATE NOT NULL,
    last_reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_user_statement (user_id, statement_id),
    KEY idx_review_next_date (user_id, next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Vocabulary book (生词本)
CREATE TABLE vocabulary_book (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    word VARCHAR(255) NOT NULL,
    chinese TEXT NULL,
    source_statement_id VARCHAR(128) NULL,
    source_course_pack_id VARCHAR(128) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vocab_user_word (user_id, word),
    KEY idx_vocab_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Daily tasks
CREATE TABLE daily_tasks (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    task_date DATE NOT NULL,
    target INT NOT NULL DEFAULT 1,
    progress INT NOT NULL DEFAULT 0,
    completed TINYINT(1) NOT NULL DEFAULT 0,
    reward_claimed TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_task (user_id, task_type, task_date),
    KEY idx_daily_tasks_user_date (user_id, task_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User total stats (cached aggregates)
CREATE TABLE user_stats (
    user_id VARCHAR(128) NOT NULL,
    total_exercises INT NOT NULL DEFAULT 0,
    total_correct INT NOT NULL DEFAULT 0,
    total_time_seconds INT NOT NULL DEFAULT 0,
    total_score INT NOT NULL DEFAULT 0,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_active_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI chat history (for in-page AI assistant)
CREATE TABLE ai_chat_history (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    statement_id VARCHAR(128) NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'user',
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ai_chat_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
