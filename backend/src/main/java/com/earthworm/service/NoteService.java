package com.earthworm.service;

import com.earthworm.config.UserContext;
import com.earthworm.model.StatementNote;
import com.earthworm.repository.StatementNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NoteService {
    private static final int MAX_CONTENT_LENGTH = 4000;
    private final StatementNoteRepository repository;

    public NoteService(StatementNoteRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> getNotes(String statementId) {
        String userId = UserContext.getUserId();
        return repository.findByUserIdAndStatementId(userId, statementId).stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> upsertNote(String statementId, String content) {
        String userId = UserContext.getUserId();
        String safeContent = content == null ? "" : content;
        if (safeContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Note content is too long");
        }
        List<StatementNote> existing = repository.findByUserIdAndStatementId(userId, statementId);
        StatementNote note;
        if (!existing.isEmpty()) {
            note = existing.get(0);
        } else {
            note = new StatementNote();
            note.setId(UUID.randomUUID().toString());
            note.setUserId(userId);
            note.setStatementId(statementId);
        }
        note.setContent(safeContent);
        repository.save(note);
        return toMap(note);
    }

    private Map<String, Object> toMap(StatementNote n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("statementId", n.getStatementId());
        m.put("content", n.getContent());
        m.put("createdAt", n.getCreatedAt() == null ? null : n.getCreatedAt().toString());
        return m;
    }
}
