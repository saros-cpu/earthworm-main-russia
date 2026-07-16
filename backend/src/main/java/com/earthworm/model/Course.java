package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Course {
    @Id
    @Column(length = 128)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String video;

    @Column(name = "`order`", nullable = false)
    private Integer order;

    @Column(name = "course_pack_id", nullable = false, length = 128, insertable = false, updatable = false)
    private String coursePackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_pack_id", nullable = false)
    private CoursePack coursePack;

    @Column(nullable = false)
    private Boolean archived = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @OrderBy("order ASC")
    private List<Statement> statements = new ArrayList<>();
}
