package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vocabulary_book")
public class VocabularyBook {
    @Id @Column(length = 128) private String id;
    @Column(name = "user_id", nullable = false, length = 128) private String userId;
    @Column(nullable = false) private String word;
    private String chinese;
    @Column(name = "source_statement_id", length = 128) private String sourceStatementId;
    @Column(name = "source_course_pack_id", length = 128) private String sourceCoursePackId;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getWord() { return word; } public void setWord(String w) { this.word = w; }
    public String getChinese() { return chinese; } public void setChinese(String c) { this.chinese = c; }
    public String getSourceStatementId() { return sourceStatementId; } public void setSourceStatementId(String s) { this.sourceStatementId = s; }
    public String getSourceCoursePackId() { return sourceCoursePackId; } public void setSourceCoursePackId(String s) { this.sourceCoursePackId = s; }
    public String getNotes() { return notes; } public void setNotes(String n) { this.notes = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
