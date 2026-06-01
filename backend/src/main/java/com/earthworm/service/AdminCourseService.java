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
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;
    private static final int MAX_RESOURCE_PATH_LENGTH = 2048;
    private static final int MAX_STATEMENT_TEXT_LENGTH = 4000;
    private static final int MAX_PHONETIC_LENGTH = 1000;
    private static final int MAX_VOCABULARY_ITEMS = 100;
    private static final Set<String> SHARE_LEVELS = Set.of("public", "private");
    private static final Set<String> DIFFICULTIES = Set.of("beginner", "elementary", "intermediate");
    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;
    private final CourseRefinementService courseRefinementService;

    public AdminCourseService(CoursePackRepository coursePackRepository, CourseRepository courseRepository,
                              StatementRepository statementRepository, CourseRefinementService courseRefinementService) {
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
        this.courseRefinementService = courseRefinementService;
    }

    public List<Map<String, Object>> coursePacks() {
        return coursePackRepository.findAll().stream().map(this::toPackItem).toList();
    }

    /**
     * Aggregated statistics for the admin dashboard + landing page.
     * Public callers only see public packs; admins retain full management totals.
     * Returns:
     *   { totals: { packs, courses, statements },
     *     packs: [ { id, title, courses, statements } sorted by statements desc ],
     *     series: [ { key, label, packs, courses, statements } ] }
     */
    public Map<String, Object> stats() {
        boolean administrator = "ADMIN".equalsIgnoreCase(UserContext.getRole());
        List<CoursePack> packs = administrator
                ? coursePackRepository.findAll()
                : coursePackRepository.findByShareLevelIgnoreCaseAndArchivedFalseOrderByOrderAsc("public");
        long totalCourses = 0;
        long totalStatements = 0;
        List<Map<String, Object>> packStats = new ArrayList<>();
        Map<String, long[]> seriesAgg = new LinkedHashMap<>(); // key → [packs, courses, stmts]
        for (CoursePack p : packs) {
            List<Course> courses = administrator
                    ? courseRepository.findByCoursePackIdOrderByOrderAsc(p.getId())
                    : courseRepository.findByCoursePackIdAndArchivedFalseOrderByOrderAsc(p.getId());
            long stmts = 0;
            for (Course c : courses) {
                stmts += administrator
                        ? statementRepository.findByCourseIdOrderByOrderAsc(c.getId()).size()
                        : statementRepository.findByCourseIdAndArchivedFalseOrderByOrderAsc(c.getId()).size();
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
        List<Statement> statements = statementRepository.findByCourseIdOrderByOrderAsc(courseId);
        Map<String, Map<String, Object>> refinements = courseRefinementService.findRefinements(
                statements.stream().map(Statement::getId).toList());
        result.put("statements", statements.stream()
                .map(statement -> toStatementItem(statement, refinements.get(statement.getId())))
                .toList());
        return result;
    }

    @Transactional
    public Map<String, Object> updateCoursePack(String id, Map<String, Object> body) {
        CoursePack pack = coursePackRepository.findById(id).orElseThrow();
        if (body.containsKey("title")) pack.setTitle(requiredTitle(body.get("title")));
        if (body.containsKey("description")) pack.setDescription(optionalText(body.get("description"), "Description", MAX_DESCRIPTION_LENGTH));
        if (body.containsKey("cover")) pack.setCover(optionalText(body.get("cover"), "Cover", MAX_RESOURCE_PATH_LENGTH));
        if (body.containsKey("shareLevel")) pack.setShareLevel(shareLevel(body.get("shareLevel")));
        if (body.containsKey("isFree")) pack.setIsFree(booleanValue(body.get("isFree"), "isFree"));
        if (body.containsKey("archived")) {
            pack.setArchived(booleanValue(body.get("archived"), "archived"));
            if (Boolean.TRUE.equals(pack.getArchived())) {
                pack.setShareLevel("private");
            }
        }
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
        course.setTitle(body.containsKey("title") ? requiredTitle(body.get("title")) : "New Course");
        course.setDescription(body.containsKey("description")
                ? optionalText(body.get("description"), "Description", MAX_DESCRIPTION_LENGTH)
                : "");
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
        stmt.setChinese(body.containsKey("sourceText")
                ? requiredText(body.get("sourceText"), "Source text", MAX_STATEMENT_TEXT_LENGTH)
                : "Please fill Chinese");
        stmt.setEnglish(body.containsKey("targetText")
                ? requiredText(body.get("targetText"), "Target text", MAX_STATEMENT_TEXT_LENGTH)
                : "Please fill Russian");
        stmt.setSoundmark(body.containsKey("phonetic")
                ? requiredText(body.get("phonetic"), "Phonetic", MAX_PHONETIC_LENGTH)
                : "");
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
        m.put("archived", Boolean.TRUE.equals(pack.getArchived()));
        return m;
    }

    private Map<String, Object> toCourseItem(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("title", c.getTitle());
        m.put("description", c.getDescription()); m.put("order", c.getOrder());
        m.put("coursePackId", c.getCoursePackId());
        m.put("video", c.getVideo());
        m.put("archived", Boolean.TRUE.equals(c.getArchived()));
        return m;
    }

    public Map<String, Object> updateCourse(String id, Map<String, Object> body) {
        Course course = courseRepository.findById(id).orElseThrow();
        if (body.containsKey("title")) course.setTitle(requiredTitle(body.get("title")));
        if (body.containsKey("description")) course.setDescription(optionalText(body.get("description"), "Description", MAX_DESCRIPTION_LENGTH));
        if (body.containsKey("video")) course.setVideo(optionalText(body.get("video"), "Video path", MAX_RESOURCE_PATH_LENGTH));
        if (body.containsKey("archived")) course.setArchived(booleanValue(body.get("archived"), "archived"));
        courseRepository.save(course);
        return toCourseItem(course);
    }

    @Transactional
    public Boolean deleteCoursePack(String id) {
        CoursePack pack = coursePackRepository.findById(id).orElseThrow();
        pack.setArchived(true);
        pack.setShareLevel("private");
        coursePackRepository.save(pack);
        return true;
    }

    @Transactional
    public Boolean deleteCourse(String id) {
        Course course = courseRepository.findById(id).orElseThrow();
        course.setArchived(true);
        courseRepository.save(course);
        return true;
    }

    @Transactional
    public Map<String, Object> updateStatement(String id, Map<String, Object> body) {
        Statement stmt = statementRepository.findById(id).orElseThrow();
        if (body.containsKey("sourceText")) stmt.setChinese(requiredText(body.get("sourceText"), "Source text", MAX_STATEMENT_TEXT_LENGTH));
        if (body.containsKey("targetText")) stmt.setEnglish(requiredText(body.get("targetText"), "Target text", MAX_STATEMENT_TEXT_LENGTH));
        if (body.containsKey("phonetic")) stmt.setSoundmark(requiredText(body.get("phonetic"), "Phonetic", MAX_PHONETIC_LENGTH));
        if (body.containsKey("archived")) stmt.setArchived(booleanValue(body.get("archived"), "archived"));
        statementRepository.save(stmt);
        if (containsRefinementFields(body)) {
            Map<String, Object> refinement = manualRefinement(stmt, body);
            saveRefinement(stmt, refinement);
            return toStatementItem(stmt, refinement);
        }
        return toStatementItem(stmt, findRefinement(id));
    }

    @Transactional
    public Boolean deleteStatement(String id) {
        Statement statement = statementRepository.findById(id).orElseThrow();
        statement.setArchived(true);
        statementRepository.save(statement);
        return true;
    }

    @Transactional
    public Map<String, Object> refineStatement(String id) {
        Statement statement = statementRepository.findById(id).orElseThrow();
        Map<String, Object> refinement = courseRefinementService.refineStatementWithRules(statement);
        saveRefinement(statement, refinement);
        return toStatementItem(statement, refinement);
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
        return toStatementItem(s, null);
    }

    private Map<String, Object> toStatementItem(Statement s, Map<String, Object> refinement) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("order", s.getOrder());
        m.put("sourceText", s.getChinese()); m.put("targetText", s.getEnglish());
        m.put("phonetic", s.getSoundmark());
        m.put("archived", Boolean.TRUE.equals(s.getArchived()));
        if (refinement != null) {
            m.putAll(refinement);
        }
        return m;
    }

    private boolean containsRefinementFields(Map<String, Object> body) {
        return body.containsKey("translation")
                || body.containsKey("vocabulary")
                || body.containsKey("grammarNote")
                || body.containsKey("difficulty");
    }

    private Map<String, Object> manualRefinement(Statement statement, Map<String, Object> body) {
        Map<String, Object> existing = findRefinement(statement.getId());
        Map<String, Object> refinement = new LinkedHashMap<>();
        refinement.put("translation", body.containsKey("translation")
                ? optionalText(body.get("translation"), "Translation", MAX_STATEMENT_TEXT_LENGTH)
                : existing.getOrDefault("translation", statement.getChinese()));
        refinement.put("vocabulary", body.containsKey("vocabulary")
                ? vocabulary(body.get("vocabulary"))
                : vocabulary(existing.get("vocabulary")));
        refinement.put("grammarNote", body.containsKey("grammarNote")
                ? optionalText(body.get("grammarNote"), "Grammar note", MAX_DESCRIPTION_LENGTH)
                : existing.get("grammarNote"));
        refinement.put("difficulty", body.containsKey("difficulty")
                ? difficulty(body.get("difficulty"))
                : existing.getOrDefault("difficulty", "beginner"));
        refinement.put("refinementMode", "rules");
        return refinement;
    }

    private Map<String, Object> findRefinement(String statementId) {
        return courseRefinementService.findRefinements(List.of(statementId))
                .getOrDefault(statementId, Map.of());
    }

    private void saveRefinement(Statement statement, Map<String, Object> refinement) {
        courseRefinementService.upsertRefinement(
                statement.getId(),
                statement.getChinese(),
                statement.getEnglish(),
                (String) refinement.get("translation"),
                vocabulary(refinement.get("vocabulary")),
                (String) refinement.get("grammarNote"),
                (String) refinement.get("difficulty"));
    }

    private List<Map<String, String>> vocabulary(Object value) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> entries) || entries.size() > MAX_VOCABULARY_ITEMS) {
            throw new IllegalArgumentException("Vocabulary must contain no more than 100 entries");
        }
        List<Map<String, String>> normalized = new ArrayList<>();
        for (Object entryValue : entries) {
            if (!(entryValue instanceof Map<?, ?> entry)) {
                throw new IllegalArgumentException("Vocabulary entries must be objects");
            }
            String word = requiredText(entry.get("word"), "Vocabulary word", MAX_TITLE_LENGTH).trim();
            if (word.isBlank()) {
                throw new IllegalArgumentException("Vocabulary word must not be blank");
            }
            Map<String, String> item = new LinkedHashMap<>();
            item.put("word", word);
            item.put("meaning", optionalText(entry.get("meaning"), "Vocabulary meaning", MAX_STATEMENT_TEXT_LENGTH));
            item.put("partOfSpeech", optionalText(entry.get("partOfSpeech"), "Part of speech", MAX_TITLE_LENGTH));
            item.put("example", optionalText(entry.get("example"), "Vocabulary example", MAX_STATEMENT_TEXT_LENGTH));
            item.put("exampleTranslation", optionalText(entry.get("exampleTranslation"), "Vocabulary example translation", MAX_STATEMENT_TEXT_LENGTH));
            normalized.add(item);
        }
        return normalized;
    }

    private String difficulty(Object value) {
        String difficulty = requiredText(value, "Difficulty", 32).toLowerCase(Locale.ROOT);
        if (!DIFFICULTIES.contains(difficulty)) {
            throw new IllegalArgumentException("Difficulty must be beginner, elementary or intermediate");
        }
        return difficulty;
    }

    private String requiredTitle(Object value) {
        String title = requiredText(value, "Title", MAX_TITLE_LENGTH).trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        return title;
    }

    private String requiredText(Object value, String field, int maximumLength) {
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(field + " must be text");
        }
        if (text.length() > maximumLength) {
            throw new IllegalArgumentException(field + " is too long");
        }
        return text;
    }

    private String optionalText(Object value, String field, int maximumLength) {
        return value == null ? null : requiredText(value, field, maximumLength);
    }

    private String shareLevel(Object value) {
        String level = requiredText(value, "Share level", 64).toLowerCase(Locale.ROOT);
        if (!SHARE_LEVELS.contains(level)) {
            throw new IllegalArgumentException("Share level must be public or private");
        }
        return level;
    }

    private Boolean booleanValue(Object value, String field) {
        if (!(value instanceof Boolean booleanValue)) {
            throw new IllegalArgumentException(field + " must be boolean");
        }
        return booleanValue;
    }

}
