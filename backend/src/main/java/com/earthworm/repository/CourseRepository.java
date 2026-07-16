package com.earthworm.repository;

import com.earthworm.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByCoursePackIdOrderByOrderAsc(String coursePackId);
    List<Course> findByCoursePackIdAndArchivedFalseOrderByOrderAsc(String coursePackId);
    List<Course> findByVideoAndArchivedFalse(String video);
    Optional<Course> findByCoursePackIdAndIdAndArchivedFalse(String coursePackId, String id);
    Optional<Course> findFirstByCoursePackIdAndArchivedFalseAndOrderGreaterThanOrderByOrderAsc(String coursePackId, Integer order);
}
