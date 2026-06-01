package com.earthworm.repository;

import com.earthworm.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, String> {
    Optional<DailyStats> findByUserIdAndDate(String userId, LocalDate date);
    List<DailyStats> findByUserIdAndDateBetweenOrderByDateAsc(String userId, LocalDate start, LocalDate end);
}
