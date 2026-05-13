package com.earthworm.service;

import com.earthworm.model.ReviewSchedule;
import com.earthworm.repository.ReviewScheduleRepository;
import com.earthworm.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {
    private final ReviewScheduleRepository reviewRepository;
    private final StatementRepository statementRepository;

    public ReviewService(ReviewScheduleRepository reviewRepository, StatementRepository statementRepository) {
        this.reviewRepository = reviewRepository;
        this.statementRepository = statementRepository;
    }

    public List<Map<String, Object>> getDueReviews(String userId) {
        List<ReviewSchedule> due = reviewRepository
                .findByUserIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(userId, LocalDate.now());
        return due.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("statementId", r.getStatementId());
            m.put("coursePackId", r.getCoursePackId());
            m.put("courseId", r.getCourseId());
            m.put("easiness", r.getEasiness());
            m.put("interval", r.getInterval());
            m.put("repetitions", r.getRepetitions());
            m.put("nextReviewAt", r.getNextReviewAt().toString());
            m.put("lastReviewedAt", r.getLastReviewedAt() == null ? null : r.getLastReviewedAt().toString());
            return m;
        }).toList();
    }

    public long getDueReviewCount(String userId) {
        return reviewRepository.countByUserIdAndNextReviewAtLessThanEqual(userId, LocalDate.now());
    }

    @Transactional
    public Map<String, Object> recordReview(String userId, String statementId, int quality) {
        ReviewSchedule schedule = reviewRepository.findByUserIdAndStatementId(userId, statementId)
                .orElse(null);
        if (schedule == null) return Map.of("error", "not found");

        quality = Math.max(0, Math.min(5, quality));

        double easiness = schedule.getEasiness();
        int interval = schedule.getInterval();
        int repetitions = schedule.getRepetitions();

        easiness = Math.max(1.3, easiness + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));

        if (quality < 3) {
            repetitions = 0;
            interval = 1;
        } else {
            if (repetitions == 0) {
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * easiness);
            }
            repetitions++;
        }

        schedule.setEasiness(easiness);
        schedule.setInterval(interval);
        schedule.setRepetitions(repetitions);
        schedule.setNextReviewAt(LocalDate.now().plusDays(interval));
        schedule.setLastReviewedAt(LocalDateTime.now());
        reviewRepository.save(schedule);

        return getDueReviewCount(userId, schedule);
    }

    @Transactional
    public Map<String, Object> scheduleReview(String userId, String statementId,
                                               String coursePackId, String courseId) {
        Optional<ReviewSchedule> existing = reviewRepository.findByUserIdAndStatementId(userId, statementId);
        if (existing.isPresent()) {
            return getDueReviewCount(userId, existing.get());
        }
        ReviewSchedule schedule = new ReviewSchedule();
        schedule.setId(UUID.randomUUID().toString());
        schedule.setUserId(userId);
        schedule.setStatementId(statementId);
        schedule.setCoursePackId(coursePackId);
        schedule.setCourseId(courseId);
        schedule.setEasiness(2.5);
        schedule.setInterval(0);
        schedule.setRepetitions(0);
        schedule.setNextReviewAt(LocalDate.now().plusDays(1));
        reviewRepository.save(schedule);
        return getDueReviewCount(userId, schedule);
    }

    private Map<String, Object> getDueReviewCount(String userId, ReviewSchedule schedule) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", schedule.getId());
        m.put("statementId", schedule.getStatementId());
        m.put("nextReviewAt", schedule.getNextReviewAt().toString());
        m.put("easiness", schedule.getEasiness());
        m.put("interval", schedule.getInterval());
        m.put("repetitions", schedule.getRepetitions());
        m.put("dueCount", getDueReviewCount(userId));
        return m;
    }
}
