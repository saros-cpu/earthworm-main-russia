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

    /**
     * Aggregated statistics for the admin dashboard + landing page.
     * Returns:
     *   { totals: { packs, courses, statements },
     *     packs: [ { id, title, courses, statements } sorted by statements desc ],
     *     series: [ { key, label, packs, courses, statements } ] }
     */
    public Map<String, Object> stats() {
        List<CoursePack> packs = coursePackRepository.findAll();
        long totalCourses = 0;
        long totalStatements = 0;
        List<Map<String, Object>> packStats = new ArrayList<>();
        Map<String, long[]> seriesAgg = new LinkedHashMap<>(); // key → [packs, courses, stmts]
        for (CoursePack p : packs) {
            List<Course> courses = courseRepository.findByCoursePackIdOrderByOrderAsc(p.getId());
            long stmts = 0;
            for (Course c : courses) {
                stmts += statementRepository.findByCourseIdOrderByOrderAsc(c.getId()).size();
            }
            totalCourses += courses.size();
            totalStatements += stmts;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", p.getId());
            row.put("title", p.getTitle());
            row.put("courses", courses.size());
            row.put("statements", stmts);
            packStats.add(row);

            String seriesKey = classifyPackSeries(p.getId(), p.getTitle());
            seriesAgg.computeIfAbsent(seriesKey, k -> new long[3]);
            long[] agg = seriesAgg.get(seriesKey);
            agg[0] += 1;
            agg[1] += courses.size();
            agg[2] += stmts;
        }
        packStats.sort((a, b) -> Long.compare((long) b.get("statements"), (long) a.get("statements")));

        List<Map<String, Object>> seriesRows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : seriesAgg.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", e.getKey());
            row.put("label", seriesLabel(e.getKey()));
            row.put("packs", e.getValue()[0]);
            row.put("courses", e.getValue()[1]);
            row.put("statements", e.getValue()[2]);
            seriesRows.add(row);
        }
        seriesRows.sort((a, b) -> Long.compare((long) b.get("statements"), (long) a.get("statements")));

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("packs", packs.size());
        totals.put("courses", totalCourses);
        totals.put("statements", totalStatements);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totals", totals);
        result.put("packs", packStats);
        result.put("series", seriesRows);
        return result;
    }

    private String classifyPackSeries(String id, String title) {
        if (id == null) id = "";
        if (title == null) title = "";
        // 考试备考：TORFL + CATTI
        if (id.startsWith("torfl-") || id.startsWith("catti-") || id.startsWith("exam-")) return "exam";
        if (id.equals("ru-baby-care") || id.equals("ru-oil-station") ||
            id.equals("ru-construction") || id.equals("ru-logistics") ||
            id.equals("ru-it-tech") || id.equals("ru-legal") ||
            id.equals("ru-medical") || id.equals("ru-trade") ||
            id.equals("ru-tourism") || id.equals("ru-education")) return "fluent";
        if (id.startsWith("ru-basic-") || title.contains("入门")) return "basic";
        if (id.startsWith("vocab-pack-") || id.startsWith("ru-grammar-") || title.contains("单词") || title.contains("词汇") || title.contains("语法")) return "grammar";
        if (id.startsWith("east-uni-") || title.contains("大学俄语")) return "textbook";
        if (title.contains("走遍") || title.contains("自学辅导")) return "textbook";
        if (id.startsWith("ru-spoken-") || title.contains("口语")) return "speaking";
        return "other";
    }

    private String seriesLabel(String key) {
        return switch (key) {
            case "basic" -> "零基础 · 入门";
            case "speaking" -> "口语会话 · 情景实战";
            case "grammar" -> "词汇语法 · 基础强化";
            case "textbook" -> "教材同步 · 课本精讲";
            case "exam" -> "俄语考级 · TORFL 与 CATTI";
            case "fluent" -> "行业俄语 · 专业应用";
            default -> "其他";
        };
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
        if (body.containsKey("cover")) pack.setCover((String) body.get("cover"));
        if (body.containsKey("shareLevel")) pack.setShareLevel((String) body.get("shareLevel"));
        if (body.containsKey("isFree")) pack.setIsFree((Boolean) body.get("isFree"));
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
        m.put("cover", pack.getCover());
        return m;
    }

    private Map<String, Object> toCourseItem(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("title", c.getTitle());
        m.put("description", c.getDescription()); m.put("order", c.getOrder());
        m.put("coursePackId", c.getCoursePackId());
        m.put("video", c.getVideo());
        return m;
    }

    public Map<String, Object> updateCourse(String id, Map<String, Object> body) {
        Course course = courseRepository.findById(id).orElseThrow();
        if (body.containsKey("title")) course.setTitle((String) body.get("title"));
        if (body.containsKey("description")) course.setDescription((String) body.get("description"));
        if (body.containsKey("video")) course.setVideo((String) body.get("video"));
        courseRepository.save(course);
        return toCourseItem(course);
    }

    @Transactional
    public Boolean deleteCoursePack(String id) {
        coursePackRepository.deleteById(id);
        return true;
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

    public int refineCourseStatements(String courseId) {
        var statements = statementRepository.findByCourseIdOrderByOrderAsc(courseId);
        int count = 0;
        for (var stmt : statements) {
            refineStatement(stmt.getId());
            count++;
        }
        return count;
    }

    private Map<String, Object> toStatementItem(Statement s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("order", s.getOrder());
        m.put("sourceText", s.getChinese()); m.put("targetText", s.getEnglish());
        m.put("phonetic", s.getSoundmark());
        return m;
    }
}
