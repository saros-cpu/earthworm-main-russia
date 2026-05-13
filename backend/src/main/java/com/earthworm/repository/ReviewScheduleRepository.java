package com.earthworm.repository;

import com.earthworm.model.ReviewSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewScheduleRepository extends JpaRepository<ReviewSchedule, String> {
    List<ReviewSchedule> findByUserIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(String userId, LocalDate date);
    Optional<ReviewSchedule> findByUserIdAndStatementId(String userId, String statementId);
    long countByUserIdAndNextReviewAtLessThanEqual(String userId, LocalDate date);
}
