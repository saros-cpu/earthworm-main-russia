package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "statements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Statement {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "`order`", nullable = false)
    private Integer order;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String chinese;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String english;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String soundmark;

    @Column(name = "course_id", nullable = false, length = 128, insertable = false, updatable = false)
    private String courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Boolean archived = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
