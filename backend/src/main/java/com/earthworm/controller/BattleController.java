package com.earthworm.controller;

import com.earthworm.config.UserContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/battle")
public class BattleController {
    private final JdbcTemplate jdbc;

    public BattleController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/create")
    public Map<String, Object> createRoom(@RequestBody Map<String, Object> body) {
        String userId = UserContext.getUserId();
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        String coursePackId = (String) body.get("coursePackId");
        jdbc.update("INSERT INTO battle_rooms (id, creator_id, course_pack_id, status) VALUES (?, ?, ?, 'waiting')",
                roomId, userId, coursePackId);
        return Map.of("roomId", roomId, "inviteCode", roomId);
    }

    @PostMapping("/join")
    public Map<String, Object> joinRoom(@RequestBody Map<String, Object> body) {
        String roomId = (String) body.get("roomId");
        String userId = UserContext.getUserId();
        int updated = jdbc.update("UPDATE battle_rooms SET opponent_id = ?, status = 'playing' WHERE id = ? AND status = 'waiting'", userId, roomId);
        if (updated == 0) return Map.of("error", "Room not found or already full");
        return Map.of("roomId", roomId, "status", "playing");
    }

    @PostMapping("/submit")
    public Map<String, Object> submitScore(@RequestBody Map<String, Object> body) {
        String roomId = (String) body.get("roomId");
        String userId = UserContext.getUserId();
        int score = (Integer) body.getOrDefault("score", 0);
        String col = jdbc.queryForObject("SELECT creator_id FROM battle_rooms WHERE id = ?", String.class, roomId);
        if (col.equals(userId)) {
            jdbc.update("UPDATE battle_rooms SET creator_score = ? WHERE id = ?", score, roomId);
        } else {
            jdbc.update("UPDATE battle_rooms SET opponent_score = ? WHERE id = ?", score, roomId);
        }
        Map<String, Object> result = jdbc.queryForMap("SELECT creator_score, opponent_score FROM battle_rooms WHERE id = ?", roomId);
        return result;
    }

    @GetMapping("/result/{roomId}")
    public Map<String, Object> getResult(@PathVariable("roomId") String roomId) {
        try {
            Map<String, Object> room = jdbc.queryForMap("SELECT * FROM battle_rooms WHERE id = ?", roomId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("roomId", roomId);
            result.put("creatorScore", room.get("creator_score"));
            result.put("opponentScore", room.get("opponent_score"));
            Number cs = (Number) room.get("creator_score");
            Number os = (Number) room.get("opponent_score");
            if (cs != null && os != null) {
                result.put("winner", cs.intValue() > os.intValue() ? "creator" : cs.intValue() < os.intValue() ? "opponent" : "draw");
            }
            return result;
        } catch (Exception e) {
            return Map.of("error", "Room not found");
        }
    }
}
