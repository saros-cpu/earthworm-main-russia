package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.service.BattleService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/battle")
public class BattleController {
    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping("/create")
    public Map<String, Object> createRoom(@RequestBody Map<String, String> body) {
        String userId = UserContext.getUserId();
        return battleService.createRoom(userId, body.getOrDefault("coursePackId", ""));
    }

    @PostMapping("/join")
    public Map<String, Object> joinRoom(@RequestBody Map<String, String> body) {
        String userId = UserContext.getUserId();
        return battleService.joinRoom(userId, body.get("roomId"));
    }

    @PostMapping("/submit")
    public Map<String, Object> submitScore(@RequestBody Map<String, Object> body) {
        String userId = UserContext.getUserId();
        return battleService.submitScore(userId, (String) body.get("roomId"), (Integer) body.get("score"));
    }

    @GetMapping("/result/{roomId}")
    public Map<String, Object> getResult(@PathVariable("roomId") String roomId) {
        return battleService.getResult(roomId);
    }

    @GetMapping("/status/{roomId}")
    public Map<String, Object> getStatus(@PathVariable("roomId") String roomId) {
        return battleService.getResult(roomId);
    }
}
