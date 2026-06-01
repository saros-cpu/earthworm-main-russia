package com.earthworm.controller;
import com.earthworm.config.UserContext;

import com.earthworm.service.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class LearningController {
    private static final long MAX_STATS_RANGE_DAYS = 366;
    private final LearningStatsService statsService;
    private final ReviewService reviewService;
    private final VocabularyService vocabularyService;
    private final DailyTaskService taskService;
    private final AiAssistantService aiService;
    private final NoteService noteService;
    private final CourseService courseService;

    public LearningController(
            LearningStatsService statsService,
            ReviewService reviewService,
            VocabularyService vocabularyService,
            DailyTaskService taskService,
            AiAssistantService aiService,
            NoteService noteService,
            CourseService courseService
    ) {
        this.statsService = statsService;
        this.reviewService = reviewService;
        this.vocabularyService = vocabularyService;
        this.taskService = taskService;
        this.aiService = aiService;
        this.noteService = noteService;
        this.courseService = courseService;
    }

    // --- Exercise Records ---
    @PostMapping("/exercise-records")
    public Map<String, Object> saveExercise(@RequestBody Map<String, Object> body) {
        courseService.requireAccessibleStatement(
                (String) body.get("coursePackId"),
                (String) body.get("courseId"),
                (String) body.get("statementId")
        );
        return Map.of("record", statsService.saveExercise(
                UserContext.getUserId(),
                (String) body.get("coursePackId"),
                (String) body.get("courseId"),
                (String) body.get("statementId"),
                Boolean.TRUE.equals(body.get("correct")),
                (Integer) body.getOrDefault("attempts", 1),
                (Integer) body.getOrDefault("timeSpentMs", 0),
                (Integer) body.getOrDefault("score", 0),
                (Integer) body.getOrDefault("combo", 0)
        ));
    }

    // --- Daily Stats ---
    @GetMapping("/stats/daily")
    public List<Map<String, Object>> dailyStats(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        LocalDate start;
        LocalDate end;
        try {
            start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Stats dates must use YYYY-MM-DD format");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Stats end date must not be before start date");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_STATS_RANGE_DAYS) {
            throw new IllegalArgumentException("Stats date range must not exceed 366 days");
        }
        return statsService.getDailyStats(UserContext.getUserId(), start, end);
    }

    @GetMapping("/stats/total")
    public Map<String, Object> totalStats() {
        return statsService.getUserStats(UserContext.getUserId());
    }

    // --- Review Schedule ---
    @GetMapping("/reviews/due")
    public List<Map<String, Object>> dueReviews() {
        return accessibleDueReviews();
    }

    @GetMapping("/reviews/due-count")
    public Map<String, Object> dueReviewCount() {
        return Map.of("count", accessibleDueReviews().size());
    }

    @PostMapping("/reviews/record")
    public Map<String, Object> recordReview(@RequestBody Map<String, Object> body) {
        String statementId = (String) body.get("statementId");
        courseService.requireAccessibleStatement(statementId);
        Map<String, Object> result = new LinkedHashMap<>(reviewService.recordReview(
                UserContext.getUserId(),
                statementId,
                (Integer) body.getOrDefault("quality", 3)
        ));
        result.put("dueCount", accessibleDueReviews().size());
        return result;
    }

    @PostMapping("/reviews/schedule")
    public Map<String, Object> scheduleReview(@RequestBody Map<String, Object> body) {
        courseService.requireAccessibleStatement(
                (String) body.get("coursePackId"),
                (String) body.get("courseId"),
                (String) body.get("statementId")
        );
        Map<String, Object> result = new LinkedHashMap<>(reviewService.scheduleReview(
                UserContext.getUserId(),
                (String) body.get("statementId"),
                (String) body.get("coursePackId"),
                (String) body.get("courseId")
        ));
        result.put("dueCount", accessibleDueReviews().size());
        return result;
    }

    // --- Vocabulary Book ---
    @GetMapping("/vocabulary")
    public List<Map<String, Object>> vocabulary() {
        return vocabularyService.getUserVocabulary(UserContext.getUserId());
    }

    @PostMapping("/vocabulary")
    public Map<String, Object> addVocabulary(@RequestBody Map<String, Object> body) {
        String sourceStatementId = (String) body.get("sourceStatementId");
        String sourceCoursePackId = (String) body.get("sourceCoursePackId");
        if (sourceStatementId != null && !sourceStatementId.isBlank()) {
            courseService.requireAccessibleStatementSource(sourceStatementId, sourceCoursePackId);
        } else if (sourceCoursePackId != null && !sourceCoursePackId.isBlank()) {
            throw new IllegalArgumentException("Vocabulary source statement is required");
        }
        return vocabularyService.addWord(
                UserContext.getUserId(),
                (String) body.get("word"),
                (String) body.get("chinese"),
                sourceStatementId,
                sourceCoursePackId,
                (String) body.get("notes")
        );
    }

    @DeleteMapping("/vocabulary")
    public Map<String, Object> removeVocabulary(@RequestParam(name = "word") String word) {
        return Map.of("removed", vocabularyService.removeWord(UserContext.getUserId(), word));
    }

    // --- Daily Tasks ---
    @GetMapping("/tasks/today")
    public Map<String, Object> todayTasks() {
        return Map.of("tasks", taskService.getTodayTasks(UserContext.getUserId()));
    }

    @PostMapping("/tasks/ensure")
    public Map<String, Object> ensureTasks() {
        return taskService.ensureTasks(UserContext.getUserId());
    }

    @PostMapping("/tasks/claim")
    public Map<String, Object> claimTaskReward(@RequestBody Map<String, Object> body) {
        return taskService.claimReward(
                UserContext.getUserId(),
                (String) body.get("taskType")
        );
    }

    // --- AI Assistant ---
    @PostMapping("/ai/ask")
    public Map<String, Object> askAi(@RequestBody Map<String, Object> body) {
        return aiService.ask(
                (String) body.get("question"),
                (String) body.get("statementId")
        );
    }

    // --- Notes ---
    @GetMapping("/notes")
    public List<Map<String, Object>> getNotes(@RequestParam(name = "statementId") String statementId) {
        courseService.requireAccessibleStatement(statementId);
        return noteService.getNotes(statementId);
    }

    @PostMapping("/notes")
    public Map<String, Object> upsertNote(@RequestBody Map<String, Object> body) {
        String statementId = (String) body.get("statementId");
        courseService.requireAccessibleStatement(statementId);
        return noteService.upsertNote(
                statementId,
                (String) body.get("content")
        );
    }

    private List<Map<String, Object>> accessibleDueReviews() {
        List<Map<String, Object>> dueReviews = reviewService.getDueReviews(UserContext.getUserId());
        Set<String> accessibleStatementIds = courseService.accessibleStatementIds(dueReviews.stream()
                .map(item -> item.get("statementId"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList());
        return dueReviews.stream()
                .filter(item -> item.get("statementId") instanceof String statementId
                        && accessibleStatementIds.contains(statementId))
                .toList();
    }
}

