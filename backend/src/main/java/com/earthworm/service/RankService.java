package com.earthworm.service;

import com.earthworm.config.UserContext;
import com.earthworm.model.User;
import com.earthworm.repository.DailyStatsRepository;
import com.earthworm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RankService {
    private final DailyStatsRepository dailyStatsRepository;
    private final UserRepository userRepository;

    public RankService(DailyStatsRepository dailyStatsRepository, UserRepository userRepository) {
        this.dailyStatsRepository = dailyStatsRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getProgressRank(String period) {
        LocalDate today = LocalDate.now();
        LocalDate start = switch (period) {
            case "monthly" -> today.minusDays(30);
            case "yearly" -> today.minusDays(365);
            default -> today.minusDays(7); // weekly
        };

        List<Object[]> rows = dailyStatsRepository.aggregateByDateRange(start, today);
        Map<String, String> userCache = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        String currentUserId = UserContext.getUserId();

        int rank = 1;
        Integer selfScore = null;
        int selfRank = 0;
        int selfExercises = 0;

        for (Object[] row : rows) {
            String userId = (String) row[0];
            Number score = (Number) row[1];
            Number exercises = (Number) row[2];
            Number timeSeconds = (Number) row[3];

            String username = userCache.computeIfAbsent(userId, uid -> {
                User u = userRepository.findById(uid).orElse(null);
                return u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : uid;
            });

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("username", username);
            item.put("count", score != null ? score.intValue() : 0);
            item.put("exercises", exercises != null ? exercises.intValue() : 0);
            item.put("timeSeconds", timeSeconds != null ? timeSeconds.intValue() : 0);
            list.add(item);

            if (userId.equals(currentUserId)) {
                selfScore = score != null ? score.intValue() : 0;
                selfRank = rank;
                selfExercises = exercises != null ? exercises.intValue() : 0;
            }
            rank++;
        }

        Map<String, Object> self = null;
        if (selfScore != null) {
            self = new LinkedHashMap<>();
            self.put("username", userCache.get(currentUserId));
            self.put("count", selfScore);
            self.put("rank", selfRank);
            self.put("exercises", selfExercises);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("self", self);
        result.put("period", period);
        return result;
    }
}