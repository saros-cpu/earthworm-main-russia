package com.earthworm.service;

import com.earthworm.model.*;
import com.earthworm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LearningStatsService {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final UserStatsRepository userStatsRepository;

    public LearningStatsService(
            ExerciseRecordRepository exerciseRecordRepository,
            DailyStatsRepository dailyStatsRepository,
            UserStatsRepository userStatsRepository
    ) {
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.dailyStatsRepository = dailyStatsRepository;
        this.userStatsRepository = userStatsRepository;
    }

    @Transactional
    public ExerciseRecord saveExercise(String userId, String coursePackId, String courseId,
                                        String statementId, boolean correct, int attempts,
                                        int timeSpentMs, int score, int combo) {
        ExerciseRecord record = new ExerciseRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setCoursePackId(coursePackId);
        record.setCourseId(courseId);
        record.setStatementId(statementId);
        record.setCorrect(correct);
        record.setAttempts(attempts);
        record.setTimeSpentMs(timeSpentMs);
        record.setScore(score);
        record.setComboAtTime(combo);
        exerciseRecordRepository.save(record);

        updateDailyStats(userId, correct, timeSpentMs / 1000, score, combo);
        updateUserStats(userId, correct, timeSpentMs / 1000, score);

        return record;
    }

    @Transactional
    public void updateDailyStats(String userId, boolean correct, int timeSeconds, int score, int combo) {
        LocalDate today = LocalDate.now();
        DailyStats stats = dailyStatsRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> {
                    DailyStats s = new DailyStats();
                    s.setId(UUID.randomUUID().toString());
                    s.setUserId(userId);
                    s.setDate(today);
                    return s;
                });
        stats.setTotalExercises(stats.getTotalExercises() == null ? 1 : stats.getTotalExercises() + 1);
        stats.setCorrectExercises(correct
                ? (stats.getCorrectExercises() == null ? 1 : stats.getCorrectExercises() + 1)
                : (stats.getCorrectExercises() == null ? 0 : stats.getCorrectExercises()));
        stats.setTotalTimeSeconds((stats.getTotalTimeSeconds() == null ? 0 : stats.getTotalTimeSeconds()) + timeSeconds);
        stats.setTotalScore((stats.getTotalScore() == null ? 0 : stats.getTotalScore()) + score);
        if (combo > (stats.getMaxCombo() == null ? 0 : stats.getMaxCombo())) {
            stats.setMaxCombo(combo);
        }
        dailyStatsRepository.save(stats);
    }

    @Transactional
    public void updateUserStats(String userId, boolean correct, int timeSeconds, int score) {
        UserStats stats = userStatsRepository.findById(userId)
                .orElseGet(() -> {
                    UserStats s = new UserStats();
                    s.setUserId(userId);
                    return s;
                });
        stats.setTotalExercises(stats.getTotalExercises() + 1);
        if (correct) stats.setTotalCorrect(stats.getTotalCorrect() + 1);
        stats.setTotalTimeSeconds(stats.getTotalTimeSeconds() + timeSeconds);
        stats.setTotalScore(stats.getTotalScore() + score);

        LocalDate today = LocalDate.now();
        if (stats.getLastActiveDate() == null) {
            stats.setCurrentStreak(1);
            stats.setLastActiveDate(today);
        } else if (stats.getLastActiveDate().equals(today.minusDays(1))) {
            stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            stats.setLastActiveDate(today);
        } else if (!stats.getLastActiveDate().equals(today)) {
            stats.setCurrentStreak(1);
            stats.setLastActiveDate(today);
        }
        if (stats.getCurrentStreak() > stats.getLongestStreak()) {
            stats.setLongestStreak(stats.getCurrentStreak());
        }
        userStatsRepository.save(stats);
    }

    public List<Map<String, Object>> getDailyStats(String userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end)
                .stream().map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("date", s.getDate().toString());
                    m.put("totalExercises", s.getTotalExercises());
                    m.put("correctExercises", s.getCorrectExercises());
                    m.put("totalTimeSeconds", s.getTotalTimeSeconds());
                    m.put("maxCombo", s.getMaxCombo());
                    m.put("totalScore", s.getTotalScore());
                    m.put("coursesCompleted", s.getCoursesCompleted());
                    return m;
                }).toList();
    }

    public Map<String, Object> getUserStats(String userId) {
        UserStats stats = userStatsRepository.findById(userId).orElse(null);
        if (stats == null) return Map.of();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalExercises", stats.getTotalExercises());
        m.put("totalCorrect", stats.getTotalCorrect());
        m.put("totalTimeSeconds", stats.getTotalTimeSeconds());
        m.put("totalScore", stats.getTotalScore());
        m.put("currentStreak", stats.getCurrentStreak());
        m.put("longestStreak", stats.getLongestStreak());
        m.put("lastActiveDate", stats.getLastActiveDate() == null ? null : stats.getLastActiveDate().toString());
        return m;
    }
}
