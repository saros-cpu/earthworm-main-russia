package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "battle_rooms")
public class BattleRoom {
    @Id @Column(length = 64) private String id;
    @Column(name = "creator_id", nullable = false, length = 128) private String creatorId;
    @Column(name = "opponent_id", nullable = true, length = 128) private String opponentId;
    @Column(name = "course_pack_id", nullable = true, length = 128) private String coursePackId;
    @Column(nullable = false, length = 32) private String status = "waiting";
    @Column(name = "creator_score", nullable = true) private Integer creatorScore;
    @Column(name = "opponent_score", nullable = true) private Integer opponentScore;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getCreatorId() { return creatorId; } public void setCreatorId(String s) { this.creatorId = s; }
    public String getOpponentId() { return opponentId; } public void setOpponentId(String s) { this.opponentId = s; }
    public String getCoursePackId() { return coursePackId; } public void setCoursePackId(String s) { this.coursePackId = s; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Integer getCreatorScore() { return creatorScore; } public void setCreatorScore(Integer i) { this.creatorScore = i; }
    public Integer getOpponentScore() { return opponentScore; } public void setOpponentScore(Integer i) { this.opponentScore = i; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
