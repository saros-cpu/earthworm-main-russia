package com.earthworm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "name", "Russian Learning Spring Boot API",
                "status", "running",
                "coursePacks", "/course-pack"
        );
    }
}
