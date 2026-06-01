package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class CourseGenerationService {
    private static final int MAX_TOPIC_LENGTH = 180;
    private static final int MAX_LEVEL_LENGTH = 32;
    private static final int MAX_ITEM_COUNT = 50;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;
    private static final int MAX_STATEMENT_TEXT_LENGTH = 4000;
    private static final int MAX_PHONETIC_LENGTH = 1000;

    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;
    private final OpenAiRefinementClient openAiRefinementClient;
    private final AdminCourseService adminCourseService;

    public CourseGenerationService(
            CoursePackRepository coursePackRepository,
            CourseRepository courseRepository,
            StatementRepository statementRepository,
            OpenAiRefinementClient openAiRefinementClient,
            AdminCourseService adminCourseService
    ) {
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
        this.openAiRefinementClient = openAiRefinementClient;
        this.adminCourseService = adminCourseService;
    }

    @Transactional
    public Map<String, Object> generateCourse(String coursePackId, Map<String, Object> body) {
        CoursePack pack = coursePackRepository.findById(coursePackId)
                .orElseThrow(() -> new NoSuchElementException("Course pack not found"));
        if (Boolean.TRUE.equals(pack.getArchived())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot generate a course for an archived course pack.");
        }

        String topic = inputText(body.get("topic"), "", "Topic", MAX_TOPIC_LENGTH);
        String level = inputText(body.get("level"), "beginner", "Level", MAX_LEVEL_LENGTH);
        int count = intValue(body.get("count"), 12);

        if (topic.isEmpty()) {
            throw new IllegalArgumentException("Topic is required for course generation");
        }
        if (count < 1 || count > MAX_ITEM_COUNT) {
            throw new IllegalArgumentException("Course generation count must be between 1 and 50");
        }

        Map<String, Object> generated = openAiRefinementClient.generateCourse(topic, level, count);
        String title = generatedText(generated.get("title"), "Generated: " + topic, MAX_TITLE_LENGTH);
        String description = generatedText(
                generated.get("description"),
                "AI generated course about " + topic,
                MAX_DESCRIPTION_LENGTH
        );
        List<GeneratedStatement> statements = validatedStatements(generated.get("items"), count);

        int nextOrder = courseRepository.findByCoursePackIdOrderByOrderAsc(coursePackId).stream()
                .map(Course::getOrder).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;

        Course course = new Course();
        course.setId("ai-course-" + UUID.randomUUID());
        course.setTitle(title);
        course.setDescription(description);
        course.setOrder(nextOrder);
        course.setCoursePack(pack);
        courseRepository.save(course);

        int stmtOrder = 1;
        for (GeneratedStatement item : statements) {
            Statement statement = new Statement();
            statement.setId("ai-stmt-" + UUID.randomUUID());
            statement.setOrder(stmtOrder++);
            statement.setChinese(item.translation());
            statement.setEnglish(item.targetText());
            statement.setSoundmark(item.phonetic());
            statement.setCourse(course);
            statementRepository.save(statement);
        }

        return adminCourseService.course(course.getId());
    }

    private String inputText(Object value, String fallback, String field, int maximumLength) {
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(field + " must be text");
        }
        String normalized = text.trim();
        if (normalized.isBlank()) {
            return fallback;
        }
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(field + " is too long");
        }
        return normalized;
    }

    private String generatedText(Object value, String fallback, int maximumLength) {
        String text = value == null ? fallback : requireGeneratedString(value);
        if (text.isBlank()) {
            text = fallback;
        }
        if (text.length() > maximumLength) {
            throw invalidResponse();
        }
        return text;
    }

    private List<GeneratedStatement> validatedStatements(Object value, int count) {
        if (!(value instanceof List<?> generatedItems) || generatedItems.isEmpty()) {
            throw invalidResponse();
        }
        List<GeneratedStatement> statements = new ArrayList<>();
        for (Object rawItem : generatedItems) {
            if (statements.size() >= count) {
                break;
            }
            if (!(rawItem instanceof Map<?, ?> item)) {
                throw invalidResponse();
            }
            String targetText = generatedText(item.get("targetText"), "", MAX_STATEMENT_TEXT_LENGTH);
            String sourceText = generatedText(item.get("sourceText"), "", MAX_STATEMENT_TEXT_LENGTH);
            String translation = generatedText(item.get("translation"), sourceText, MAX_STATEMENT_TEXT_LENGTH);
            String phonetic = generatedText(item.get("phonetic"), "", MAX_PHONETIC_LENGTH);
            if (targetText.isBlank() || translation.isBlank()) {
                throw invalidResponse();
            }
            statements.add(new GeneratedStatement(translation, targetText, phonetic));
        }
        if (statements.isEmpty()) {
            throw invalidResponse();
        }
        return statements;
    }

    private String requireGeneratedString(Object value) {
        if (!(value instanceof String text)) {
            throw invalidResponse();
        }
        return text.trim();
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "AI course generation returned invalid content.");
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number n) return n.intValue();
        if (value != null) {
            try { return Integer.parseInt(value.toString()); } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    private record GeneratedStatement(String translation, String targetText, String phonetic) {}
}
