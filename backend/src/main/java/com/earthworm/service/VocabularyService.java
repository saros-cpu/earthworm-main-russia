package com.earthworm.service;

import com.earthworm.model.VocabularyBook;
import com.earthworm.repository.VocabularyBookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getWords(String userId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VocabularyBook> result;
        if (search != null && !search.isBlank()) {
            result = repository.searchByUserId(userId, search.trim(), pageable);
        } else {
            result = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream().map(this::toMapVerbose).toList());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("number", result.getNumber());
        response.put("size", result.getSize());
        return response;
    }

    public Map<String, Object> getWordById(String userId, String wordId) {
        VocabularyBook v = repository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("Word not found: " + wordId));
        if (!v.getUserId().equals(userId)) {
            throw new NoSuchElementException("Word not found: " + wordId);
        }
        return toMapVerbose(v);
    }

    @Transactional
    public Map<String, Object> updateWord(String userId, String wordId, Map<String, Object> updates) {
        VocabularyBook v = repository.findById(wordId)
                .orElseThrow(() -> new NoSuchElementException("Word not found: " + wordId));
        if (!v.getUserId().equals(userId)) {
            throw new NoSuchElementException("Word not found: " + wordId);
        }
        if (updates.containsKey("chinese")) v.setChinese((String) updates.get("chinese"));
        if (updates.containsKey("partOfSpeech")) v.setPartOfSpeech((String) updates.get("partOfSpeech"));
        if (updates.containsKey("phonetic")) v.setPhonetic((String) updates.get("phonetic"));
        if (updates.containsKey("exampleSentence")) v.setExampleSentence((String) updates.get("exampleSentence"));
        if (updates.containsKey("exampleTranslation")) v.setExampleTranslation((String) updates.get("exampleTranslation"));
        if (updates.containsKey("studyLevel")) v.setStudyLevel((Integer) updates.get("studyLevel"));
        if (updates.containsKey("notes")) v.setNotes((String) updates.get("notes"));
        repository.save(v);
        return toMapVerbose(v);
    }

    @Transactional
    public Map<String, Object> addWord(String userId, String word, String chinese,
                                        String sourceStatementId, String sourceCoursePackId, String notes) {
        String normalizedWord = normalizeWord(word);
        if (chinese != null && chinese.length() > 2000) {
            throw new IllegalArgumentException("Vocabulary meaning is too long");
        }
        if (notes != null && notes.length() > 4000) {
            throw new IllegalArgumentException("Vocabulary notes are too long");
        }
        Optional<VocabularyBook> existing = repository.findByUserIdAndWord(userId, normalizedWord);
        if (existing.isPresent()) {
            VocabularyBook v = existing.get();
            v.setNotes(notes != null ? notes : v.getNotes());
            repository.save(v);
            return toMap(v);
        }
        VocabularyBook v = new VocabularyBook();
        v.setId(UUID.randomUUID().toString());
        v.setUserId(userId);
        v.setWord(normalizedWord);
        v.setChinese(chinese);
        v.setSourceStatementId(sourceStatementId);
        v.setSourceCoursePackId(sourceCoursePackId);
        v.setNotes(notes);
        repository.save(v);
        return toMap(v);
    }

    @Transactional
    public boolean removeWord(String userId, String word) {
        repository.deleteByUserIdAndWord(userId, normalizeWord(word));
        return true;
    }

    private String normalizeWord(String word) {
        String normalizedWord = word == null ? "" : word.trim();
        if (normalizedWord.isBlank() || normalizedWord.length() > 255) {
            throw new IllegalArgumentException("Vocabulary word must be between 1 and 255 characters");
        }
        return normalizedWord;
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

    private Map<String, Object> toMapVerbose(VocabularyBook v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("word", v.getWord());
        m.put("chinese", v.getChinese());
        m.put("partOfSpeech", v.getPartOfSpeech());
        m.put("phonetic", v.getPhonetic());
        m.put("exampleSentence", v.getExampleSentence());
        m.put("exampleTranslation", v.getExampleTranslation());
        m.put("studyLevel", v.getStudyLevel());
        m.put("sourceStatementId", v.getSourceStatementId());
        m.put("sourceCoursePackId", v.getSourceCoursePackId());
        m.put("notes", v.getNotes());
        m.put("createdAt", v.getCreatedAt() == null ? null : v.getCreatedAt().toString());
        return m;
    }
}
