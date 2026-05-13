package com.earthworm.repository;

import com.earthworm.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByCoursePackIdOrderByOrderAsc(String coursePackId);
    Optional<Course> findByCoursePackIdAndId(String coursePackId, String id);
    Optional<Course> findByCoursePackIdAndOrder(String coursePackId, Integer order);
}
