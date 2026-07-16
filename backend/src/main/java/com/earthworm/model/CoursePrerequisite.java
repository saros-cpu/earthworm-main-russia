package com.earthworm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_prerequisites")
@IdClass(CoursePrerequisiteId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CoursePrerequisite {
    @Id
    @Column(name = "course_id", length = 128, nullable = false)
    private String courseId;

    @Id
    @Column(name = "required_course_id", length = 128, nullable = false)
    private String requiredCourseId;
}
