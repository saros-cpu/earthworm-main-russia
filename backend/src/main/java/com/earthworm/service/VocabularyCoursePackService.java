package com.earthworm.service;

import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VocabularyCoursePackService {
    private final CoursePackRepository coursePackRepository;
    private final StatementRepository statementRepository;

    public VocabularyCoursePackService(CoursePackRepository coursePackRepository, StatementRepository statementRepository) {
        this.coursePackRepository = coursePackRepository;
        this.statementRepository = statementRepository;
    }

    public Map<String, Object> generate(Map<String, Object> body) {
        return Map.of("coursePackId", "", "title", "Vocabulary Pack", "courseCount", 0, "wordCount", 0, "withMeaningCount", 0);
    }

    public Map<String, Object> refreshPrompts(String coursePackId) {
        return Map.of("coursePackId", coursePackId, "statementCount", 0, "withMeaningCount", 0);
    }

    public Map<String, Object> enrichVocabulary(String coursePackId, Map<String, Object> body) {
        return Map.of("coursePackId", coursePackId, "candidateCount", 0, "enrichedCount", 0, "aiAvailable", false);
    }

    public Map<String, Object> organizeCourses(String coursePackId) {
        return Map.of("coursePackId", coursePackId, "courseCount", 0, "statementCount", 0, "buckets", List.of());
    }
}
