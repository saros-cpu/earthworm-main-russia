CREATE TABLE course_prerequisites (
    course_id VARCHAR(128) NOT NULL,
    required_course_id VARCHAR(128) NOT NULL,
    PRIMARY KEY (course_id, required_course_id),
    CONSTRAINT fk_prereq_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_prereq_required FOREIGN KEY (required_course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Default prerequisites: within each course pack, course(order=N) requires course(order=N-1)
INSERT IGNORE INTO course_prerequisites (course_id, required_course_id)
SELECT c2.id, c1.id
FROM courses c1
JOIN courses c2 ON c1.course_pack_id = c2.course_pack_id AND c2.order = c1.order + 1
WHERE NOT c1.archived AND NOT c2.archived;
