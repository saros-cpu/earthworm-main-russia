package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "statement_notes")
public class StatementNote {
    @Id @Column(length = 128) private String id;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(name = "statement_id", nullable = false, length = 128) private String statementId;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { this.userId = v; }
    public String getStatementId() { return statementId; } public void setStatementId(String v) { this.statementId = v; }
    public String getContent() { return content; } public void setContent(String v) { this.content = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
