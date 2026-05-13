package com.earthworm.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class RankController {
    @GetMapping("/rank/progress/{period}")
    public Map<String, Object> progress(@PathVariable("period") String period) {
        return Map.of(
                "self", Map.of("username", "dev-user", "count", 0, "rank", -1),
                "list", List.of()
        );
    }
}
