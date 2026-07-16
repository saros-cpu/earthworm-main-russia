package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_packs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CoursePack {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "`order`", nullable = false)
    private Integer order;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_free")
    private Boolean isFree;

    @Column(columnDefinition = "TEXT")
    private String cover;

    @Column(name = "creator_id", nullable = false, columnDefinition = "TEXT")
    private String creatorId;

    @Column(name = "share_level", length = 64)
    private String shareLevel;

    @Column(nullable = false)
    private Boolean archived = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "coursePack", fetch = FetchType.LAZY)
    @OrderBy("order ASC")
    private List<Course> courses = new ArrayList<>();
}
