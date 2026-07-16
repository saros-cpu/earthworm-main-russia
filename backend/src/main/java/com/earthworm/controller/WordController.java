package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.service.VocabularyService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class WordController {
    private final VocabularyService vocabularyService;

    public WordController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping("/words")
    public Map<String, Object> listWords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return vocabularyService.getWords(UserContext.getUserId(), page, size, search);
    }

    @GetMapping("/words/{id}")
    public Map<String, Object> getWord(@PathVariable String id) {
        return vocabularyService.getWordById(UserContext.getUserId(), id);
    }

    @PatchMapping("/words/{id}")
    public Map<String, Object> updateWord(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return vocabularyService.updateWord(UserContext.getUserId(), id, updates);
    }

    @DeleteMapping("/words/{id}")
    public Map<String, Object> deleteWord(@PathVariable String id) {
        vocabularyService.removeWord(UserContext.getUserId(), id);
        return Map.of("removed", true);
    }
}
