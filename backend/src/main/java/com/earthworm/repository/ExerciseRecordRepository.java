package com.earthworm.repository;

import com.earthworm.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, String> {
    List<ExerciseRecord> findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(String userId, LocalDateTime start, LocalDateTime end);
    long countByUserId(String userId);
    long countByUserIdAndCorrect(String userId, Boolean correct);
}
