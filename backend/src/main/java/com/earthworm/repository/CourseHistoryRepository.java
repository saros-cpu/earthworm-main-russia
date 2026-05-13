package com.earthworm.repository;

import com.earthworm.model.CourseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseHistoryRepository extends JpaRepository<CourseHistory, String> {
    List<CourseHistory> findByUserIdAndCoursePackId(String userId, String coursePackId);
    Optional<CourseHistory> findByUserIdAndCoursePackIdAndCourseId(String userId, String coursePackId, String courseId);
}
