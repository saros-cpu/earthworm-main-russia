package com.earthworm.repository;

import com.earthworm.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatsRepository extends JpaRepository<DailyStats, String> {
    Optional<DailyStats> findByUserIdAndDate(String userId, LocalDate date);
    List<DailyStats> findByUserIdAndDateBetweenOrderByDateAsc(String userId, LocalDate start, LocalDate end);

    @Query(value = "SELECT d.user_id, SUM(d.total_score) AS total_score, SUM(d.total_exercises) AS total_exercises, SUM(d.total_time_seconds) AS total_time_seconds " +
           "FROM daily_stats d WHERE d.date BETWEEN :start AND :end GROUP BY d.user_id ORDER BY total_score DESC",
           nativeQuery = true)
    List<Object[]> aggregateByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
