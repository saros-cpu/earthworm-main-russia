package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_schedule")
public class ReviewSchedule {
    @Id @Column(length = 128) private String id;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(name = "statement_id", nullable = false, length = 128) private String statementId;
    @Column(name = "course_pack_id", nullable = false, length = 128) private String coursePackId;
    @Column(name = "course_id", nullable = false, length = 128) private String courseId;
    @Column(nullable = false) private Double easiness = 2.5;
    @Column(name = "`interval`", nullable = false) private Integer interval = 0;
    @Column(nullable = false) private Integer repetitions = 0;
    @Column(name = "next_review_at", nullable = false) private LocalDate nextReviewAt;
    @Column(name = "last_reviewed_at") private LocalDateTime lastReviewedAt;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getStatementId() { return statementId; } public void setStatementId(String s) { this.statementId = s; }
    public String getCoursePackId() { return coursePackId; } public void setCoursePackId(String s) { this.coursePackId = s; }
    public String getCourseId() { return courseId; } public void setCourseId(String s) { this.courseId = s; }
    public Double getEasiness() { return easiness; } public void setEasiness(Double d) { this.easiness = d; }
    public Integer getInterval() { return interval; } public void setInterval(Integer i) { this.interval = i; }
    public Integer getRepetitions() { return repetitions; } public void setRepetitions(Integer i) { this.repetitions = i; }
    public LocalDate getNextReviewAt() { return nextReviewAt; } public void setNextReviewAt(LocalDate d) { this.nextReviewAt = d; }
    public LocalDateTime getLastReviewedAt() { return lastReviewedAt; } public void setLastReviewedAt(LocalDateTime d) { this.lastReviewedAt = d; }
}
