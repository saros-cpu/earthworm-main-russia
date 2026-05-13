package com.earthworm.service;

import com.earthworm.config.UserContext;
import com.earthworm.model.StatementNote;
import com.earthworm.repository.StatementNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NoteService {
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
        note.setContent(content);
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
