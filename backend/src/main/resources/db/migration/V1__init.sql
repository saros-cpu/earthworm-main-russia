CREATE TABLE course_packs (
    id VARCHAR(128) NOT NULL,
    `order` INT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NULL,
    is_free TINYINT(1) NULL,
    cover TEXT NULL,
    creator_id TEXT NOT NULL,
    share_level VARCHAR(64) DEFAULT 'private',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE courses (
    id VARCHAR(128) NOT NULL,
    title TEXT NOT NULL,
    description TEXT NULL,
    video TEXT NULL,
    `order` INT NOT NULL,
    course_pack_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_courses_course_pack_id (course_pack_id),
    CONSTRAINT fk_courses_course_pack_id
        FOREIGN KEY (course_pack_id) REFERENCES course_packs (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE statements (
    id VARCHAR(128) NOT NULL,
    `order` INT NOT NULL,
    chinese TEXT NOT NULL,
    english TEXT NOT NULL,
    soundmark TEXT NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_statements_course_id (course_id),
    CONSTRAINT fk_statements_course_id
        FOREIGN KEY (course_id) REFERENCES courses (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE statement_refinements (
    statement_id VARCHAR(128) NOT NULL,
    source_text TEXT NULL,
    target_text TEXT NOT NULL,
    translation TEXT NULL,
    vocabulary_json TEXT NULL,
    grammar_note TEXT NULL,
    difficulty VARCHAR(32) NULL,
    refinement_mode VARCHAR(32) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (statement_id),
    CONSTRAINT fk_statement_refinements_statement_id
        FOREIGN KEY (statement_id) REFERENCES statements (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE course_history (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    course_pack_id VARCHAR(128) NOT NULL,
    completion_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_history_user_course_pack (user_id, course_id, course_pack_id),
    KEY idx_course_history_course_id (course_id),
    KEY idx_course_history_course_pack_id (course_pack_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_course_progress (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    course_pack_id VARCHAR(128) NOT NULL,
    course_id VARCHAR(128) NOT NULL,
    statement_index INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_course_progress_user_pack (user_id, course_pack_id),
    KEY idx_user_course_progress_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mastered_elements (
    id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    content JSON NOT NULL,
    mastered_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_mastered_elements_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pdf_import_jobs (
    id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    title TEXT NULL,
    filename TEXT NULL,
    progress INT NOT NULL DEFAULT 0,
    message TEXT NULL,
    result_json JSON NULL,
    course_pack_id VARCHAR(128) NULL,
    error_message TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_pdf_import_jobs_status (status),
    KEY idx_pdf_import_jobs_course_pack_id (course_pack_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
