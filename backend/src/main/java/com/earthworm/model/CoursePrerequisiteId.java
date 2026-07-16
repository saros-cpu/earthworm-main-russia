package com.earthworm.model;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class CoursePrerequisiteId implements Serializable {
    private String courseId;
    private String requiredCourseId;
}
