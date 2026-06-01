package com.earthworm.repository;

import com.earthworm.model.UserCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, String> {
    Optional<UserCourseProgress> findByUserIdAndCoursePackId(String userId, String coursePackId);
    Optional<UserCourseProgress> findByUserIdAndCoursePackIdAndCourseId(String userId, String coursePackId, String courseId);
    List<UserCourseProgress> findTop5ByUserIdOrderByUpdatedAtDesc(String userId);
}
