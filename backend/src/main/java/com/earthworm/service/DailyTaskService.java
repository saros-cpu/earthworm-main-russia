package com.earthworm.service;

import com.earthworm.model.DailyTask;
import com.earthworm.repository.DailyTaskRepository;
import com.earthworm.repository.DailyStatsRepository;
import com.earthworm.model.DailyStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class DailyTaskService {
    private static final Map<String, Integer> REWARD_SCORES = Map.of(
            "complete_10", 50,
            "combo_5", 100,
            "learn_15min", 50
    );

    private final DailyTaskRepository repository;
    private final DailyStatsRepository dailyStatsRepository;

    public DailyTaskService(DailyTaskRepository repository, DailyStatsRepository dailyStatsRepository) {
        this.repository = repository;
        this.dailyStatsRepository = dailyStatsRepository;
    }

    public List<Map<String, Object>> getTodayTasks(String userId) {
        return repository.findByUserIdAndTaskDate(userId, LocalDate.now())
                .stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> ensureTasks(String userId) {
        LocalDate today = LocalDate.now();
        List<DailyTask> existing = repository.findByUserIdAndTaskDate(userId, today);
        if (!existing.isEmpty()) {
            return Map.of("tasks", existing.stream().map(this::toMap).toList());
        }

        List<Map<String, Object>> created = new ArrayList<>();
        for (var def : getDefaultTaskDefs()) {
            DailyTask task = new DailyTask();
            task.setId(UUID.randomUUID().toString());
            task.setUserId(userId);
            task.setTaskType(def.type());
            task.setTaskDate(today);
            task.setTarget(def.target());
            task.setProgress(0);
            task.setCompleted(false);
            task.setRewardClaimed(false);
            repository.save(task);
            created.add(toMap(task));
        }
        return Map.of("tasks", created);
    }

    @Transactional
    public Map<String, Object> updateProgress(String userId, String taskType, int increment) {
        LocalDate today = LocalDate.now();
        DailyTask task = repository.findByUserIdAndTaskTypeAndTaskDate(userId, taskType, today)
                .orElseGet(() -> {
                    DailyTask t = new DailyTask();
                    t.setId(UUID.randomUUID().toString());
                    t.setUserId(userId);
                    t.setTaskType(taskType);
                    t.setTaskDate(today);
                    t.setTarget(1);
                    t.setProgress(0);
                    t.setCompleted(false);
                    t.setRewardClaimed(false);
                    return t;
                });
        task.setProgress(task.getProgress() + increment);
        if (task.getProgress() >= task.getTarget()) {
            task.setCompleted(true);
        }
        repository.save(task);
        return toMap(task);
    }

    @Transactional
    public Map<String, Object> claimReward(String userId, String taskType) {
        LocalDate today = LocalDate.now();
        DailyTask task = repository.findByUserIdAndTaskTypeAndTaskDate(userId, taskType, today)
                .orElse(null);
        if (task != null && task.getCompleted() && !task.getRewardClaimed()) {
            task.setRewardClaimed(true);
            repository.save(task);

            int bonusScore = REWARD_SCORES.getOrDefault(taskType, 0);
            if (bonusScore > 0) {
                DailyStats stats = dailyStatsRepository.findByUserIdAndDate(userId, today)
                        .orElseGet(() -> {
                            DailyStats s = new DailyStats();
                            s.setId(UUID.randomUUID().toString());
                            s.setUserId(userId);
                            s.setDate(today);
                            return s;
                        });
                stats.setTotalScore((stats.getTotalScore() == null ? 0 : stats.getTotalScore()) + bonusScore);
                dailyStatsRepository.save(stats);
            }
        }
        return task != null ? toMap(task) : Map.of();
    }

    private record TaskDef(String type, int target) {}
    private List<TaskDef> getDefaultTaskDefs() {
        return List.of(
                new TaskDef("complete_10", 10),
                new TaskDef("combo_5", 5),
                new TaskDef("learn_15min", 15)
        );
    }

    private Map<String, Object> toMap(DailyTask t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("taskType", t.getTaskType());
        m.put("taskDate", t.getTaskDate().toString());
        m.put("target", t.getTarget());
        m.put("progress", t.getProgress());
        m.put("completed", t.getCompleted());
        m.put("rewardClaimed", t.getRewardClaimed());
        return m;
    }
}
