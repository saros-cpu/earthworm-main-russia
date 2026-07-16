package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_group_members")
public class StudyGroupMember {
    @Id @Column(length = 128) private String id;
    @Column(name = "group_id", nullable = false, length = 128) private String groupId;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(nullable = false, length = 32) private String role = "member";
    @Column(name = "joined_at", insertable = false, updatable = false) private LocalDateTime joinedAt;

    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public String getGroupId() { return groupId; } public void setGroupId(String v) { this.groupId = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { this.userId = v; }
    public String getRole() { return role; } public void setRole(String v) { this.role = v; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}