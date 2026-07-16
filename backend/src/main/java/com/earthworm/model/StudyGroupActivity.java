package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_group_activities")
public class StudyGroupActivity {
    @Id @Column(length = 128) private String id;
    @Column(name = "group_id", nullable = false, length = 128) private String groupId;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(name = "activity_type", nullable = false, length = 64) private String activityType;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "metadata_json", columnDefinition = "TEXT") private String metadataJson;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;

    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public String getGroupId() { return groupId; } public void setGroupId(String v) { this.groupId = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { this.userId = v; }
    public String getActivityType() { return activityType; } public void setActivityType(String v) { this.activityType = v; }
    public String getDescription() { return description; } public void setDescription(String v) { this.description = v; }
    public String getMetadataJson() { return metadataJson; } public void setMetadataJson(String v) { this.metadataJson = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}