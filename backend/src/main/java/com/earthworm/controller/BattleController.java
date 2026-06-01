package com.earthworm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/battle")
public class BattleController {
    @PostMapping("/create")
    public Map<String, Object> createRoom(@RequestBody Map<String, Object> body) {
        throw unavailable();
    }

    @PostMapping("/join")
    public Map<String, Object> joinRoom(@RequestBody Map<String, Object> body) {
        throw unavailable();
    }

    @PostMapping("/submit")
    public Map<String, Object> submitScore(@RequestBody Map<String, Object> body) {
        throw unavailable();
    }

    @GetMapping("/result/{roomId}")
    public Map<String, Object> getResult(@PathVariable("roomId") String roomId) {
        throw unavailable();
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Battle mode is unavailable until scores can be verified on the server.");
    }
}
