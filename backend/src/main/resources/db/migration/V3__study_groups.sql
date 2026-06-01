-- Study groups
CREATE TABLE study_groups (
    id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    cover TEXT NULL,
    creator_id VARCHAR(128) NOT NULL,
    member_count INT NOT NULL DEFAULT 1,
    invite_code VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invite_code (invite_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Group members
CREATE TABLE study_group_members (
    id VARCHAR(128) NOT NULL,
    group_id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'member',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_member (group_id, user_id),
    KEY idx_group_members_group (group_id),
    KEY idx_group_members_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Group activity feed
CREATE TABLE study_group_activities (
    id VARCHAR(128) NOT NULL,
    group_id VARCHAR(128) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    activity_type VARCHAR(64) NOT NULL,
    description TEXT NULL,
    metadata_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_group_activities_group (group_id),
    KEY idx_group_activities_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
