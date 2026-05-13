package com.earthworm.controller;

import com.earthworm.config.UserContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class RankController {
    private final JdbcTemplate jdbcTemplate;

    public RankController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/rank/progress/{period}")
    public Map<String, Object> progress(@PathVariable("period") String period) {
        String userId = UserContext.getUserId();
        String dateFilter = switch (period) {
            case "weekly" -> "AND us.date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "monthly" -> "AND us.date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            default -> "AND us.date = CURDATE()";
        };

        List<Map<String, Object>> list = jdbcTemplate.query(
            "SELECT us.user_id, COALESCE(u.nickname, us.user_id) as username, " +
            "SUM(us.total_score) as total_score, SUM(us.total_exercises) as total_exercises, " +
            "MAX(us.max_combo) as max_combo " +
            "FROM daily_stats us " +
            "LEFT JOIN users u ON u.id = us.user_id " +
            "WHERE 1=1 " + dateFilter + " " +
            "GROUP BY us.user_id ORDER BY total_score DESC LIMIT 20",
            (rs, rowNum) -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", rs.getString("user_id"));
                item.put("username", rs.getString("username"));
                item.put("score", rs.getInt("total_score"));
                item.put("count", rs.getInt("total_exercises"));
                item.put("maxCombo", rs.getInt("max_combo"));
                item.put("rank", rowNum + 1);
                return item;
            }
        );

        Map<String, Object> self = Map.of("username", "dev-user", "count", 0, "rank", -1);
        if (!list.isEmpty()) {
            int rank = 1;
            for (Map<String, Object> item : list) {
                if (userId.equals(item.get("userId"))) {
                    self = item;
                    break;
                }
                rank++;
            }
        }

        return Map.of("self", self, "list", list);
    }
}
