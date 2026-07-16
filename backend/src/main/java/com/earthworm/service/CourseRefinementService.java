package com.earthworm.service;

import com.earthworm.model.Statement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CourseRefinementService {
    private static final Pattern RUSSIAN_WORD = Pattern.compile("[\\p{IsCyrillic}]+");

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public CourseRefinementService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Map<String, Object>> findRefinements(List<String> statementIds) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        if (statementIds.isEmpty()) return result;

        String placeholders = String.join(",", statementIds.stream().map(id -> "?").toList());
        String sql = "SELECT statement_id, source_text, target_text, translation, vocabulary_json, grammar_note, difficulty, refinement_mode FROM statement_refinements WHERE statement_id IN (" + placeholders + ")";

        jdbcTemplate.query(sql, (java.sql.ResultSet rs) -> {
            String stmtId = rs.getString("statement_id");
            Map<String, Object> refinement = new LinkedHashMap<>();
            refinement.put("refinementMode", rs.getString("refinement_mode"));
            refinement.put("translation", rs.getString("translation"));
            refinement.put("grammarNote", rs.getString("grammar_note"));
            refinement.put("difficulty", rs.getString("difficulty"));

            String vocabJson = rs.getString("vocabulary_json");
            if (vocabJson != null && !vocabJson.isBlank()) {
                try {
                    refinement.put("vocabulary", objectMapper.readValue(vocabJson, new TypeReference<List<Map<String, String>>>() {}));
                } catch (Exception ignored) {}
            }
            result.put(stmtId, refinement);
        }, statementIds.toArray());
        return result;
    }

    public void upsertRefinement(String statementId, String sourceText, String targetText, String translation,
                                  List<Map<String, String>> vocabulary, String grammarNote, String difficulty) {
        try {
            String vocabJson = objectMapper.writeValueAsString(vocabulary);
            jdbcTemplate.update(
                "INSERT INTO statement_refinements (statement_id, source_text, target_text, translation, vocabulary_json, grammar_note, difficulty, refinement_mode) VALUES (?, ?, ?, ?, ?, ?, ?, 'rules') ON DUPLICATE KEY UPDATE source_text=VALUES(source_text), target_text=VALUES(target_text), translation=VALUES(translation), vocabulary_json=VALUES(vocabulary_json), grammar_note=VALUES(grammar_note), difficulty=VALUES(difficulty), refinement_mode='rules'",
                statementId, sourceText, targetText, translation, vocabJson, grammarNote, difficulty
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Vocabulary data could not be saved", exception);
        }
    }

    public void deleteRefinement(String statementId) {
        jdbcTemplate.update("DELETE FROM statement_refinements WHERE statement_id = ?", statementId);
    }

    public Map<String, Object> refineStatementWithRules(Statement statement) {
        Map<String, Object> refinement = new LinkedHashMap<>();
        refinement.put("translation", statement.getChinese());
        refinement.put("grammarNote", extractGrammarNote(statement.getEnglish()));
        refinement.put("difficulty", "beginner");
        refinement.put("refinementMode", "rules");

        List<Map<String, String>> vocabulary = extractVocabulary(statement.getEnglish(), statement.getChinese());
        refinement.put("vocabulary", vocabulary);
        return refinement;
    }

    private String extractGrammarNote(String text) {
        List<String> notes = new ArrayList<>();
        Matcher m = RUSSIAN_WORD.matcher(text);
        while (m.find()) {
            String word = m.group();
            if (word.endsWith("\u0441\u044F") || word.endsWith("\u0441\u044C")) {
                notes.add(word + " - reflexive verb");
            }
        }
        return notes.isEmpty() ? "" : String.join("; ", notes);
    }

    private List<Map<String, String>> extractVocabulary(String russian, String chinese) {
        List<Map<String, String>> vocab = new ArrayList<>();
        Matcher m = RUSSIAN_WORD.matcher(russian);
        while (m.find()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("word", m.group());
            entry.put("meaning", "");
            entry.put("partOfSpeech", inferPartOfSpeech(m.group()));
            vocab.add(entry);
        }
        return vocab;
    }

    private String inferPartOfSpeech(String word) {
        if (word.endsWith("\u044C") || word.endsWith("\u0430") || word.endsWith("\u044F")) return "noun";
        if (word.endsWith("\u0442\u044C") || word.endsWith("\u0447\u044C") || word.endsWith("\u0442\u0438")) return "verb";
        if (word.endsWith("\u044B\u0439") || word.endsWith("\u0438\u0439") || word.endsWith("\u043E\u0439")) return "adjective";
        if (word.endsWith("\u043E") || word.endsWith("\u0435")) return "adverb";
        return "other";
    }
}
