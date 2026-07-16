package com.earthworm.controller;

import com.earthworm.service.RankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RankController {
    private final RankService rankService;

    public RankController(RankService rankService) {
        this.rankService = rankService;
    }

    @GetMapping("/rank/progress/{period}")
    public Map<String, Object> progress(@PathVariable("period") String period) {
        return rankService.getProgressRank(period);
    }
}