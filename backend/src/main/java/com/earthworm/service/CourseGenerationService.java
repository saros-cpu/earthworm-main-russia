package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseGenerationService {
    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;
    private final CourseRefinementService refinementService;
    private final OpenAiRefinementClient openAiRefinementClient;
    private final AdminCourseService adminCourseService;

    public CourseGenerationService(
            CoursePackRepository coursePackRepository,
            CourseRepository courseRepository,
            StatementRepository statementRepository,
            CourseRefinementService refinementService,
            OpenAiRefinementClient openAiRefinementClient,
            AdminCourseService adminCourseService
    ) {
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
        this.refinementService = refinementService;
        this.openAiRefinementClient = openAiRefinementClient;
        this.adminCourseService = adminCourseService;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateCourse(String coursePackId, Map<String, Object> body) {
        CoursePack pack = coursePackRepository.findById(coursePackId)
                .orElseThrow(() -> new NoSuchElementException("Course pack not found"));

        String topic = stringValue(body.get("topic"), "");
        String level = stringValue(body.get("level"), "beginner");
        int count = intValue(body.get("count"), 12);

        if (topic.isEmpty()) {
            throw new IllegalArgumentException("Topic is required for course generation");
        }

        Map<String, Object> generated = openAiRefinementClient.generateCourse(topic, level, count);
        String title = stringValue(generated.get("title"), "Generated: " + topic);
        String description = stringValue(generated.get("description"), "AI generated course about " + topic);

        int nextOrder = courseRepository.findByCoursePackIdOrderByOrderAsc(coursePackId).stream()
                .map(Course::getOrder).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;

        Course course = new Course();
        course.setId("ai-course-" + UUID.randomUUID());
        course.setTitle(title);
        course.setDescription(description);
        course.setOrder(nextOrder);
        course.setCoursePack(pack);
        courseRepository.save(course);

        List<Map<String, Object>> items = (List<Map<String, Object>>) generated.getOrDefault("items", List.of());
        int stmtOrder = 1;
        for (Map<String, Object> item : items) {
            if (stmtOrder > count) break;
            String translation = stringValue(item.get("translation"), stringValue(item.get("sourceText"), "translation pending"));
            Statement statement = new Statement();
            statement.setId("ai-stmt-" + UUID.randomUUID());
            statement.setOrder(stmtOrder++);
            statement.setChinese(translation);
            statement.setEnglish(stringValue(item.get("targetText"), "text pending"));
            statement.setSoundmark(stringValue(item.get("phonetic"), ""));
            statement.setCourse(course);
            statementRepository.save(statement);
        }

        return adminCourseService.course(course.getId());
    }

    private String stringValue(Object value, String fallback) {
        if (value == null) return fallback;
        String s = value.toString();
        return s.isBlank() ? fallback : s;
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number n) return n.intValue();
        if (value != null) {
            try { return Integer.parseInt(value.toString()); } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }
}
