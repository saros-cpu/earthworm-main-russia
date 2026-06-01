package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id @Column(name = "user_id", length = 128) private String userId;
    @Column(name = "total_exercises") private Integer totalExercises = 0;
    @Column(name = "total_correct") private Integer totalCorrect = 0;
    @Column(name = "total_time_seconds") private Integer totalTimeSeconds = 0;
    @Column(name = "total_score") private Integer totalScore = 0;
    @Column(name = "current_streak") private Integer currentStreak = 0;
    @Column(name = "longest_streak") private Integer longestStreak = 0;
    @Column(name = "last_active_date") private LocalDate lastActiveDate;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getUserId() { return userId; } public void setUserId(String id) { this.userId = id; }
    public Integer getTotalExercises() { return totalExercises; } public void setTotalExercises(Integer v) { this.totalExercises = v; }
    public Integer getTotalCorrect() { return totalCorrect; } public void setTotalCorrect(Integer v) { this.totalCorrect = v; }
    public Integer getTotalTimeSeconds() { return totalTimeSeconds; } public void setTotalTimeSeconds(Integer v) { this.totalTimeSeconds = v; }
    public Integer getTotalScore() { return totalScore; } public void setTotalScore(Integer v) { this.totalScore = v; }
    public Integer getCurrentStreak() { return currentStreak; } public void setCurrentStreak(Integer v) { this.currentStreak = v; }
    public Integer getLongestStreak() { return longestStreak; } public void setLongestStreak(Integer v) { this.longestStreak = v; }
    public LocalDate getLastActiveDate() { return lastActiveDate; } public void setLastActiveDate(LocalDate d) { this.lastActiveDate = d; }
}
