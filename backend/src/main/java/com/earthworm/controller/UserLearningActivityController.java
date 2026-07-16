package com.earthworm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class UserLearningActivityController {
    @GetMapping("/user-learning-activities")
    public List<Map<String, Object>> dailyTotals() {
        throw removed();
    }

    @PostMapping("/user-learning-activities")
    public Map<String, Object> upsert(@RequestBody Map<String, Object> body) {
        throw removed();
    }

    @GetMapping("/user-learning-activities/total")
    public Map<String, Object> total() {
        throw removed();
    }

    private ResponseStatusException removed() {
        return new ResponseStatusException(HttpStatus.GONE, "Use authenticated learning statistics endpoints instead.");
    }
}
