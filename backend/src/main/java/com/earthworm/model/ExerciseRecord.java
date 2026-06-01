package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_records")
public class ExerciseRecord {
    @Id
    @Column(length = 128)
    private String id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "course_pack_id", nullable = false, length = 128)
    private String coursePackId;

    @Column(name = "course_id", nullable = false, length = 128)
    private String courseId;

    @Column(name = "statement_id", nullable = false, length = 128)
    private String statementId;

    @Column(nullable = false)
    private Boolean correct = false;

    @Column(nullable = false)
    private Integer attempts = 1;

    @Column(name = "time_spent_ms", nullable = false)
    private Integer timeSpentMs = 0;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "combo_at_time", nullable = false)
    private Integer comboAtTime = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCoursePackId() { return coursePackId; }
    public void setCoursePackId(String coursePackId) { this.coursePackId = coursePackId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getStatementId() { return statementId; }
    public void setStatementId(String statementId) { this.statementId = statementId; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public Integer getTimeSpentMs() { return timeSpentMs; }
    public void setTimeSpentMs(Integer timeSpentMs) { this.timeSpentMs = timeSpentMs; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getComboAtTime() { return comboAtTime; }
    public void setComboAtTime(Integer comboAtTime) { this.comboAtTime = comboAtTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
