package com.earthworm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class RankController {
    @GetMapping("/rank/progress/{period}")
    public Map<String, Object> progress(@PathVariable("period") String period) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Ranking is unavailable until exercise results can be verified on the server.");
    }
}
