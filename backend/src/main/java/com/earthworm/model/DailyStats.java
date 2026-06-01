package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_stats")
public class DailyStats {
    @Id @Column(length = 128) private String id;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(nullable = false) private LocalDate date;
    @Column(name = "total_exercises", nullable = false) private Integer totalExercises = 0;
    @Column(name = "correct_exercises", nullable = false) private Integer correctExercises = 0;
    @Column(name = "total_time_seconds", nullable = false) private Integer totalTimeSeconds = 0;
    @Column(name = "max_combo", nullable = false) private Integer maxCombo = 0;
    @Column(name = "total_score", nullable = false) private Integer totalScore = 0;
    @Column(name = "courses_completed", nullable = false) private Integer coursesCompleted = 0;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public LocalDate getDate() { return date; } public void setDate(LocalDate date) { this.date = date; }
    public Integer getTotalExercises() { return totalExercises; } public void setTotalExercises(Integer v) { this.totalExercises = v; }
    public Integer getCorrectExercises() { return correctExercises; } public void setCorrectExercises(Integer v) { this.correctExercises = v; }
    public Integer getTotalTimeSeconds() { return totalTimeSeconds; } public void setTotalTimeSeconds(Integer v) { this.totalTimeSeconds = v; }
    public Integer getMaxCombo() { return maxCombo; } public void setMaxCombo(Integer v) { this.maxCombo = v; }
    public Integer getTotalScore() { return totalScore; } public void setTotalScore(Integer v) { this.totalScore = v; }
    public Integer getCoursesCompleted() { return coursesCompleted; } public void setCoursesCompleted(Integer v) { this.coursesCompleted = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
