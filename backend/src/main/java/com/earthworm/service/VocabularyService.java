package com.earthworm.service;

import com.earthworm.model.VocabularyBook;
import com.earthworm.repository.VocabularyBookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VocabularyService {
    private final VocabularyBookRepository repository;

    public VocabularyService(VocabularyBookRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> getUserVocabulary(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("word", v.getWord());
            m.put("chinese", v.getChinese());
            m.put("notes", v.getNotes());
            m.put("createdAt", v.getCreatedAt() == null ? null : v.getCreatedAt().toString());
            return m;
        }).toList();
    }

    @Transactional
    public Map<String, Object> addWord(String userId, String word, String chinese,
                                        String sourceStatementId, String sourceCoursePackId, String notes) {
        Optional<VocabularyBook> existing = repository.findByUserIdAndWord(userId, word);
        if (existing.isPresent()) {
            VocabularyBook v = existing.get();
            v.setNotes(notes != null ? notes : v.getNotes());
            repository.save(v);
            return toMap(v);
        }
        VocabularyBook v = new VocabularyBook();
        v.setId(UUID.randomUUID().toString());
        v.setUserId(userId);
        v.setWord(word);
        v.setChinese(chinese);
        v.setSourceStatementId(sourceStatementId);
        v.setSourceCoursePackId(sourceCoursePackId);
        v.setNotes(notes);
        repository.save(v);
        return toMap(v);
    }

    @Transactional
    public boolean removeWord(String userId, String word) {
        repository.deleteByUserIdAndWord(userId, word);
        return true;
    }

    private Map<String, Object> toMap(VocabularyBook v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("word", v.getWord());
        m.put("chinese", v.getChinese());
        m.put("notes", v.getNotes());
        m.put("createdAt", v.getCreatedAt() == null ? null : v.getCreatedAt().toString());
        return m;
    }
}
