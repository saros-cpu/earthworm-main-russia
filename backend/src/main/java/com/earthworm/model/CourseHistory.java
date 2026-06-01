package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseHistory {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "course_id", nullable = false, length = 128)
    private String courseId;

    @Column(name = "course_pack_id", nullable = false, length = 128)
    private String coursePackId;

    @Column(name = "completion_count", nullable = false)
    private Integer completionCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
