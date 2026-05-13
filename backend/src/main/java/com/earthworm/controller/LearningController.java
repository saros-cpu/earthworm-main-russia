package com.earthworm.controller;
import com.earthworm.config.UserContext;

import com.earthworm.service.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class LearningController {
    private final LearningStatsService statsService;
    private final ReviewService reviewService;
    private final VocabularyService vocabularyService;
    private final DailyTaskService taskService;
    private final AiAssistantService aiService;
    private final NoteService noteService;

    public LearningController(
            LearningStatsService statsService,
            ReviewService reviewService,
            VocabularyService vocabularyService,
            DailyTaskService taskService,
            AiAssistantService aiService,
            NoteService noteService
    ) {
        this.statsService = statsService;
        this.reviewService = reviewService;
        this.vocabularyService = vocabularyService;
        this.taskService = taskService;
        this.aiService = aiService;
        this.noteService = noteService;
    }

    // --- Exercise Records ---
    @PostMapping("/exercise-records")
    public Map<String, Object> saveExercise(@RequestBody Map<String, Object> body) {
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
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        return statsService.getDailyStats(UserContext.getUserId(), start, end);
    }

    @GetMapping("/stats/total")
    public Map<String, Object> totalStats() {
        return statsService.getUserStats(UserContext.getUserId());
    }

    // --- Review Schedule ---
    @GetMapping("/reviews/due")
    public List<Map<String, Object>> dueReviews() {
        return reviewService.getDueReviews(UserContext.getUserId());
    }

    @GetMapping("/reviews/due-count")
    public Map<String, Object> dueReviewCount() {
        return Map.of("count", reviewService.getDueReviewCount(UserContext.getUserId()));
    }

    @PostMapping("/reviews/record")
    public Map<String, Object> recordReview(@RequestBody Map<String, Object> body) {
        return reviewService.recordReview(
                UserContext.getUserId(),
                (String) body.get("statementId"),
                (Integer) body.getOrDefault("quality", 3)
        );
    }

    @PostMapping("/reviews/schedule")
    public Map<String, Object> scheduleReview(@RequestBody Map<String, Object> body) {
        return reviewService.scheduleReview(
                UserContext.getUserId(),
                (String) body.get("statementId"),
                (String) body.get("coursePackId"),
                (String) body.get("courseId")
        );
    }

    // --- Vocabulary Book ---
    @GetMapping("/vocabulary")
    public List<Map<String, Object>> vocabulary() {
        return vocabularyService.getUserVocabulary(UserContext.getUserId());
    }

    @PostMapping("/vocabulary")
    public Map<String, Object> addVocabulary(@RequestBody Map<String, Object> body) {
        return vocabularyService.addWord(
                UserContext.getUserId(),
                (String) body.get("word"),
                (String) body.get("chinese"),
                (String) body.get("sourceStatementId"),
                (String) body.get("sourceCoursePackId"),
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

    @PostMapping("/tasks/progress")
    public Map<String, Object> updateTaskProgress(@RequestBody Map<String, Object> body) {
        return taskService.updateProgress(
                UserContext.getUserId(),
                (String) body.get("taskType"),
                (Integer) body.getOrDefault("increment", 1)
        );
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
        return noteService.getNotes(statementId);
    }

    @PostMapping("/notes")
    public Map<String, Object> upsertNote(@RequestBody Map<String, Object> body) {
        return noteService.upsertNote(
                (String) body.get("statementId"),
                (String) body.get("content")
        );
    }
}

