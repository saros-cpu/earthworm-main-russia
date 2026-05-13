package com.earthworm.service;

import com.earthworm.config.UserContext;
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
public class AdminCourseService {
    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;

    public AdminCourseService(CoursePackRepository coursePackRepository, CourseRepository courseRepository, StatementRepository statementRepository) {
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
    }

    public List<Map<String, Object>> coursePacks() {
        return coursePackRepository.findAll().stream().map(this::toPackItem).toList();
    }

    public Map<String, Object> coursePack(String id) {
        CoursePack pack = coursePackRepository.findById(id).orElseThrow();
        Map<String, Object> result = toPackItem(pack);
        result.put("courses", courseRepository.findByCoursePackIdOrderByOrderAsc(id).stream().map(this::toCourseItem).toList());
        return result;
    }

    public Map<String, Object> course(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        Map<String, Object> result = toCourseItem(course);
        result.put("statements", statementRepository.findByCourseIdOrderByOrderAsc(courseId).stream().map(s -> toStatementItem(s)).toList());
        return result;
    }

    @Transactional
    public Map<String, Object> updateCoursePack(String id, Map<String, Object> body) {
        CoursePack pack = coursePackRepository.findById(id).orElseThrow();
        if (body.containsKey("title")) pack.setTitle((String) body.get("title"));
        if (body.containsKey("description")) pack.setDescription((String) body.get("description"));
        coursePackRepository.save(pack);
        return toPackItem(pack);
    }

    @Transactional
    public Map<String, Object> createCourse(String coursePackId, Map<String, Object> body) {
        CoursePack pack = coursePackRepository.findById(coursePackId).orElseThrow();
        int nextOrder = courseRepository.findByCoursePackIdOrderByOrderAsc(coursePackId).stream()
                .map(Course::getOrder).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
        Course course = new Course();
        course.setId("course-" + UUID.randomUUID());
        course.setTitle((String) body.getOrDefault("title", "New Course"));
        course.setDescription((String) body.getOrDefault("description", ""));
        course.setOrder(nextOrder);
        course.setCoursePack(pack);
        courseRepository.save(course);
        return toCourseItem(course);
    }

    @Transactional
    public Map<String, Object> createStatement(String courseId, Map<String, Object> body) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        int nextOrder = statementRepository.findByCourseIdOrderByOrderAsc(courseId).stream()
                .map(Statement::getOrder).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
        Statement stmt = new Statement();
        stmt.setId("stmt-" + UUID.randomUUID());
        stmt.setOrder(nextOrder);
        stmt.setChinese((String) body.getOrDefault("sourceText", "Please fill Chinese"));
        stmt.setEnglish((String) body.getOrDefault("targetText", "Please fill Russian"));
        stmt.setSoundmark((String) body.getOrDefault("phonetic", ""));
        stmt.setCourse(course);
        statementRepository.save(stmt);
        return toStatementItem(stmt);
    }

    private Map<String, Object> toPackItem(CoursePack pack) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", pack.getId()); m.put("title", pack.getTitle());
        m.put("description", pack.getDescription()); m.put("order", pack.getOrder());
        m.put("shareLevel", pack.getShareLevel()); m.put("isFree", pack.getIsFree());
        return m;
    }

    private Map<String, Object> toCourseItem(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("title", c.getTitle());
        m.put("description", c.getDescription()); m.put("order", c.getOrder());
        m.put("coursePackId", c.getCoursePackId());
        return m;
    }

    public Map<String, Object> updateCourse(String id, Map<String, Object> body) {
        Course course = courseRepository.findById(id).orElseThrow();
        if (body.containsKey("title")) course.setTitle((String) body.get("title"));
        if (body.containsKey("description")) course.setDescription((String) body.get("description"));
        courseRepository.save(course);
        return toCourseItem(course);
    }

    public Boolean deleteCourse(String id) {
        courseRepository.deleteById(id);
        return true;
    }

    public Map<String, Object> updateStatement(String id, Map<String, Object> body) {
        Statement stmt = statementRepository.findById(id).orElseThrow();
        if (body.containsKey("sourceText")) stmt.setChinese((String) body.get("sourceText"));
        if (body.containsKey("targetText")) stmt.setEnglish((String) body.get("targetText"));
        if (body.containsKey("phonetic")) stmt.setSoundmark((String) body.get("phonetic"));
        statementRepository.save(stmt);
        return toStatementItem(stmt);
    }

    public Boolean deleteStatement(String id) {
        statementRepository.deleteById(id);
        return true;
    }

    public Map<String, Object> refineStatement(String id) {
        return toStatementItem(statementRepository.findById(id).orElseThrow());
    }

    private Map<String, Object> toStatementItem(Statement s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("order", s.getOrder());
        m.put("sourceText", s.getChinese()); m.put("targetText", s.getEnglish());
        m.put("phonetic", s.getSoundmark());
        return m;
    }
}
