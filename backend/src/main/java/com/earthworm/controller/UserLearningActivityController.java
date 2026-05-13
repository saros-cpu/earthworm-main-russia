package com.earthworm.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserLearningActivityController {
    @GetMapping("/user-learning-activities")
    public List<Map<String, Object>> dailyTotals() {
        return List.of();
    }

    @PostMapping("/user-learning-activities")
    public Map<String, Object> upsert(@RequestBody Map<String, Object> body) {
        return body;
    }

    @GetMapping("/user-learning-activities/total")
    public Map<String, Object> total() {
        return Map.of("total", 0);
    }
}
