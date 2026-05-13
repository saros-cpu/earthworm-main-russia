package com.earthworm.repository;

import com.earthworm.model.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyTaskRepository extends JpaRepository<DailyTask, String> {
    Optional<DailyTask> findByUserIdAndTaskTypeAndTaskDate(String userId, String taskType, LocalDate date);
    List<DailyTask> findByUserIdAndTaskDate(String userId, LocalDate date);
}
