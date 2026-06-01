package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_tasks")
public class DailyTask {
    @Id @Column(length = 128) private String id;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(name = "task_type", nullable = false, length = 64) private String taskType;
    @Column(name = "task_date", nullable = false) private LocalDate taskDate;
    @Column(nullable = false) private Integer target = 1;
    @Column(nullable = false) private Integer progress = 0;
    @Column(nullable = false) private Boolean completed = false;
    @Column(name = "reward_claimed", nullable = false) private Boolean rewardClaimed = false;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String id) { this.userId = id; }
    public String getTaskType() { return taskType; } public void setTaskType(String t) { this.taskType = t; }
    public LocalDate getTaskDate() { return taskDate; } public void setTaskDate(LocalDate d) { this.taskDate = d; }
    public Integer getTarget() { return target; } public void setTarget(Integer t) { this.target = t; }
    public Integer getProgress() { return progress; } public void setProgress(Integer p) { this.progress = p; }
    public Boolean getCompleted() { return completed; } public void setCompleted(Boolean b) { this.completed = b; }
    public Boolean getRewardClaimed() { return rewardClaimed; } public void setRewardClaimed(Boolean b) { this.rewardClaimed = b; }
}
