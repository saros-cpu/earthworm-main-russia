package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_groups")
public class StudyGroup {
    @Id @Column(length = 128) private String id;
    @Column(nullable = false) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(columnDefinition = "TEXT") private String cover;
    @Column(name = "creator_id", nullable = false, length = 128) private String creatorId;
    @Column(name = "member_count") private Integer memberCount = 1;
    @Column(name = "invite_code", length = 64, unique = true) private String inviteCode;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getDescription() { return description; } public void setDescription(String v) { this.description = v; }
    public String getCover() { return cover; } public void setCover(String v) { this.cover = v; }
    public String getCreatorId() { return creatorId; } public void setCreatorId(String v) { this.creatorId = v; }
    public Integer getMemberCount() { return memberCount; } public void setMemberCount(Integer v) { this.memberCount = v; }
    public String getInviteCode() { return inviteCode; } public void setInviteCode(String v) { this.inviteCode = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
