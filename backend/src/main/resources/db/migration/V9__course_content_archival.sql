ALTER TABLE course_packs
    ADD COLUMN archived TINYINT(1) NOT NULL DEFAULT 0 AFTER share_level,
    ADD KEY idx_course_packs_visibility (`share_level`, archived, `order`);

ALTER TABLE courses
    ADD COLUMN archived TINYINT(1) NOT NULL DEFAULT 0 AFTER course_pack_id,
    ADD KEY idx_courses_pack_archived_order (course_pack_id, archived, `order`);

ALTER TABLE statements
    ADD COLUMN archived TINYINT(1) NOT NULL DEFAULT 0 AFTER course_id,
    ADD KEY idx_statements_course_archived_order (course_id, archived, `order`);
