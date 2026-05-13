package com.earthworm.repository;

import com.earthworm.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStats, String> {
}
