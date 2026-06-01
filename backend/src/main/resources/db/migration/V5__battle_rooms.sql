CREATE TABLE battle_rooms (
    id VARCHAR(64) NOT NULL,
    creator_id VARCHAR(128) NOT NULL,
    opponent_id VARCHAR(128) NULL,
    course_pack_id VARCHAR(128) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'waiting',
    creator_score INT NULL,
    opponent_score INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_battle_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
