package com.earthworm.repository;

import com.earthworm.model.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, String> {
    @Query("SELECT p.requiredCourseId FROM CoursePrerequisite p WHERE p.courseId = :courseId")
    List<String> findRequiredCourseIdsByCourseId(@Param("courseId") String courseId);
}
