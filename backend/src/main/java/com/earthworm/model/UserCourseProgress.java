package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_course_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserCourseProgress {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "course_pack_id", nullable = false, length = 128)
    private String coursePackId;

    @Column(name = "course_id", nullable = false, length = 128)
    private String courseId;

    @Column(name = "statement_index", nullable = false)
    private Integer statementIndex;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
