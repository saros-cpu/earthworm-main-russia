package com.earthworm.repository;

import com.earthworm.model.AiDailyBudget;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AiDailyBudgetRepository extends JpaRepository<AiDailyBudget, LocalDate> {
    @Modifying
    @Query(value = """
            INSERT INTO ai_daily_budget (usage_date, reserved_output_tokens, request_count)
            VALUES (:usageDate, 0, 0)
            ON DUPLICATE KEY UPDATE usage_date = usage_date
            """, nativeQuery = true)
    void ensureUsageDateExists(@Param("usageDate") LocalDate usageDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select usage from AiDailyBudget usage where usage.usageDate = :usageDate")
    Optional<AiDailyBudget> findByUsageDateForUpdate(@Param("usageDate") LocalDate usageDate);
}
