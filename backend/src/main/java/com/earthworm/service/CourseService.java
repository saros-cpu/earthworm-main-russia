package com.earthworm.service;

import com.earthworm.config.CurrentUser;
import com.earthworm.config.UserContext;
import com.earthworm.model.*;
import com.earthworm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseService {
    private final CurrentUser currentUser;
    private final CoursePackRepository coursePackRepository;
    private final CourseRepository courseRepository;
    private final StatementRepository statementRepository;
    private final UserCourseProgressRepository progressRepository;
    private final CourseHistoryRepository historyRepository;
    private final CourseRefinementService courseRefinementService;

    public CourseService(
            CurrentUser currentUser,
            CoursePackRepository coursePackRepository,
            CourseRepository courseRepository,
            StatementRepository statementRepository,
            UserCourseProgressRepository progressRepository,
            CourseHistoryRepository historyRepository,
            CourseRefinementService courseRefinementService
    ) {
        this.currentUser = currentUser;
        this.coursePackRepository = coursePackRepository;
        this.courseRepository = courseRepository;
        this.statementRepository = statementRepository;
        this.progressRepository = progressRepository;
        this.historyRepository = historyRepository;
        this.courseRefinementService = courseRefinementService;
    }

    private String userId() {
        return currentUser.getUserId();
    }

    public List<Map<String, Object>> findCoursePacks() {
        return coursePackRepository.findByShareLevelIgnoreCaseAndArchivedFalseOrderByOrderAsc("public")
                .stream()
                .map(this::toCoursePackItem)
                .toList();
    }

    public Map<String, Object> findCoursePack(String coursePackId) {
        CoursePack pack = requireAccessiblePack(coursePackId);

        List<Map<String, Object>> courses = courseRepository.findByCoursePackIdAndArchivedFalseOrderByOrderAsc(coursePackId)
                .stream()
                .map(course -> toCourseItem(course, completionCount(coursePackId, course.getId())))
                .toList();

        Map<String, Object> result = toCoursePackItem(pack);
        result.put("courses", courses);
        return result;
    }

    public Map<String, Object> findCourse(String coursePackId, String courseId) {
        requireAccessiblePack(coursePackId);
        Course course = courseRepository.findByCoursePackIdAndIdAndArchivedFalse(coursePackId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        List<Statement> courseStatements = statementRepository.findByCourseIdAndArchivedFalseOrderByOrderAsc(courseId);
        Map<String, Map<String, Object>> refinements = courseRefinementService.findRefinements(
                courseStatements.stream().map(Statement::getId).toList()
        );
        List<Map<String, Object>> statements = courseStatements.stream()
                .map(statement -> toStatementItem(statement, refinements.get(statement.getId())))
                .toList();

        Map<String, Object> result = toCourseItem(course, completionCount(coursePackId, courseId));
        result.put("statements", statements);
        result.put("statementIndex", statementIndex(coursePackId, courseId));
        return result;
    }

    public Map<String, Object> findNextCourse(String coursePackId, String courseId) {
        requireAccessiblePack(coursePackId);
        Course current = courseRepository.findByCoursePackIdAndIdAndArchivedFalse(coursePackId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        return courseRepository.findFirstByCoursePackIdAndArchivedFalseAndOrderGreaterThanOrderByOrderAsc(coursePackId, current.getOrder())
                .map(course -> toCourseItem(course, completionCount(coursePackId, course.getId())))
                .orElse(null);
    }

    @Transactional
    public Map<String, Object> completeCourse(String coursePackId, String courseId) {
        String uid = UserContext.getUserIdOptional().orElse(null);
        if (uid == null) {
            return Map.of("error", "Login required");
        }
        requireAccessiblePack(coursePackId);
        Course current = courseRepository.findByCoursePackIdAndIdAndArchivedFalse(coursePackId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        CourseHistory history = historyRepository
                .findByUserIdAndCoursePackIdAndCourseId(uid, coursePackId, courseId)
                .orElseGet(() -> {
                    CourseHistory entity = new CourseHistory();
                    entity.setId(UUID.randomUUID().toString());
                    entity.setUserId(uid);
                    entity.setCoursePackId(coursePackId);
                    entity.setCourseId(courseId);
                    entity.setCompletionCount(0);
                    return entity;
                });
        history.setCompletionCount(history.getCompletionCount() + 1);
        historyRepository.save(history);

        Course next = courseRepository.findFirstByCoursePackIdAndArchivedFalseAndOrderGreaterThanOrderByOrderAsc(coursePackId, current.getOrder())
                .orElse(null);
        if (next != null) {
            upsertProgress(coursePackId, next.getId(), 0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nextCourse", next == null ? null : toCourseItem(next, completionCount(coursePackId, next.getId())));
        return result;
    }

    @Transactional
    public Map<String, Object> upsertProgress(String coursePackId, String courseId, Integer statementIndex) {
        requireAccessiblePack(coursePackId);
        Course course = courseRepository.findByCoursePackIdAndIdAndArchivedFalse(coursePackId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        long statementCount = statementRepository.countByCourseIdAndArchivedFalse(courseId);
        int maximumIndex = (int) Math.max(0L, Math.min(Integer.MAX_VALUE, statementCount - 1));
        int safeStatementIndex = Math.max(0, Math.min(statementIndex == null ? 0 : statementIndex, maximumIndex));
        UserCourseProgress progress = progressRepository
                .findByUserIdAndCoursePackId(userId(), coursePackId)
                .orElseGet(() -> {
                    UserCourseProgress entity = new UserCourseProgress();
                    entity.setId(UUID.randomUUID().toString());
                    entity.setUserId(userId());
                    entity.setCoursePackId(coursePackId);
                    return entity;
                });

        progress.setCourseId(course.getId());
        progress.setStatementIndex(safeStatementIndex);
        progressRepository.save(progress);

        return Map.of("courseId", course.getId());
    }

    public void requireAccessibleStatement(String coursePackId, String courseId, String statementId) {
        requireAccessiblePack(coursePackId);
        courseRepository.findByCoursePackIdAndIdAndArchivedFalse(coursePackId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        if (statementId == null || !statementRepository.existsByIdAndCourseIdAndArchivedFalse(statementId, courseId)) {
            throw new NoSuchElementException("Statement not found");
        }
    }

    public void requireAccessibleStatement(String statementId) {
        requireAccessibleStatementSource(statementId, null);
    }

    public boolean canAccessStatement(String statementId) {
        try {
            requireAccessibleStatement(statementId);
            return true;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    public Set<String> accessibleStatementIds(Collection<String> statementIds) {
        List<String> requestedIds = statementIds == null
                ? List.of()
                : statementIds.stream().filter(Objects::nonNull).distinct().toList();
        if (requestedIds.isEmpty()) {
            return Set.of();
        }
        String uid = UserContext.getUserIdOptional().orElse("");
        boolean administrator = "ADMIN".equalsIgnoreCase(UserContext.getRole());
        return Set.copyOf(statementRepository.findAccessibleIds(requestedIds, uid, administrator));
    }

    public void requireAccessibleStatementSource(String statementId, String expectedCoursePackId) {
        if (statementId == null || statementId.isBlank()) {
            throw new NoSuchElementException("Statement not found");
        }
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new NoSuchElementException("Statement not found"));
        if (Boolean.TRUE.equals(statement.getArchived())) {
            throw new NoSuchElementException("Statement not found");
        }
        String courseId = statement.getCourseId();
        if (courseId == null || courseId.isBlank()) {
            throw new NoSuchElementException("Statement not found");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        if (Boolean.TRUE.equals(course.getArchived())) {
            throw new NoSuchElementException("Course not found");
        }
        String coursePackId = course.getCoursePackId();
        if (coursePackId == null || coursePackId.isBlank()) {
            throw new NoSuchElementException("Course pack not found");
        }
        requireAccessiblePack(coursePackId);
        if (expectedCoursePackId != null
                && !expectedCoursePackId.isBlank()
                && !expectedCoursePackId.equals(coursePackId)) {
            throw new NoSuchElementException("Statement not found");
        }
    }

    public List<Map<String, Object>> recentCoursePacks() {
        return progressRepository.findTop5ByUserIdOrderByUpdatedAtDesc(userId())
                .stream()
                .map(progress -> coursePackRepository.findById(progress.getCoursePackId())
                        .filter(this::canAccess)
                        .map(pack -> {
                            Map<String, Object> item = toCoursePackItem(pack);
                            item.put("coursePackId", progress.getCoursePackId());
                            item.put("courseId", progress.getCourseId());
                            return item;
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Map<String, Object>> courseHistory(String coursePackId) {
        requireAccessiblePack(coursePackId);
        return historyRepository.findByUserIdAndCoursePackId(userId(), coursePackId)
                .stream()
                .map(history -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("courseId", history.getCourseId());
                    item.put("completionCount", history.getCompletionCount());
                    return item;
                })
                .toList();
    }

    public boolean canStreamMedia(String mediaPath) {
        if (mediaPath == null || mediaPath.isBlank()) {
            return false;
        }
        return courseRepository.findByVideoAndArchivedFalse(mediaPath).stream()
                .map(course -> coursePackRepository.findById(course.getCoursePackId()).orElse(null))
                .filter(Objects::nonNull)
                .anyMatch(this::canAccess);
    }

    private CoursePack requireAccessiblePack(String coursePackId) {
        CoursePack pack = coursePackRepository.findById(coursePackId)
                .orElseThrow(() -> new NoSuchElementException("Course pack not found"));
        if (!canAccess(pack)) {
            throw new NoSuchElementException("Course pack not found");
        }
        return pack;
    }

    private boolean canAccess(CoursePack pack) {
        if (Boolean.TRUE.equals(pack.getArchived())) {
            return false;
        }
        if ("public".equalsIgnoreCase(pack.getShareLevel())) {
            return true;
        }
        if ("ADMIN".equalsIgnoreCase(UserContext.getRole())) {
            return true;
        }
        return UserContext.getUserIdOptional()
                .map(uid -> uid.equals(pack.getCreatorId()))
                .orElse(false);
    }

    private Integer statementIndex(String coursePackId, String courseId) {
        return UserContext.getUserIdOptional()
                .flatMap(uid -> progressRepository
                        .findByUserIdAndCoursePackIdAndCourseId(uid, coursePackId, courseId)
                        .map(UserCourseProgress::getStatementIndex))
                .orElse(0);
    }

    private Integer completionCount(String coursePackId, String courseId) {
        return UserContext.getUserIdOptional()
                .flatMap(uid -> historyRepository
                        .findByUserIdAndCoursePackIdAndCourseId(uid, coursePackId, courseId)
                        .map(CourseHistory::getCompletionCount))
                .orElse(0);
    }

    private Map<String, Object> toCoursePackItem(CoursePack pack) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", pack.getId());
        item.put("title", pack.getTitle());
        item.put("description", pack.getDescription());
        item.put("isFree", Boolean.TRUE.equals(pack.getIsFree()));
        item.put("cover", pack.getCover());
        return item;
    }

    private Map<String, Object> toCourseItem(Course course, Integer completionCount) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", course.getId());
        item.put("title", course.getTitle());
        item.put("description", course.getDescription());
        item.put("order", course.getOrder());
        item.put("coursePackId", course.getCoursePackId());
        item.put("completionCount", completionCount);
        item.put("statementIndex", 0);
        item.put("statementCount", statementRepository.countByCourseIdAndArchivedFalse(course.getId()));
        item.put("video", course.getVideo());
        String lyrics = CustomCoursePackService.LYRICS_CACHE.get(course.getId());
        if (lyrics != null) item.put("lyrics", lyrics);
        return item;
    }

    private Map<String, Object> toStatementItem(Statement statement) {
        return toStatementItem(statement, null);
    }

    private Map<String, Object> toStatementItem(Statement statement, Map<String, Object> refinement) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", statement.getId());
        item.put("order", statement.getOrder());
        item.put("chinese", statement.getChinese());
        item.put("english", statement.getEnglish());
        item.put("soundmark", statement.getSoundmark());
        item.put("sourceText", statement.getChinese());
        item.put("targetText", statement.getEnglish());
        item.put("phonetic", statement.getSoundmark());
        item.put("language", "ru");
        if (refinement != null) {
            item.putAll(refinement);
            Object translation = refinement.get("translation");
            if (translation != null && !translation.toString().isBlank()) {
                item.put("chinese", translation);
                item.put("sourceText", translation);
            }
        }
        item.put("isMastered", false);
        return item;
    }
}
