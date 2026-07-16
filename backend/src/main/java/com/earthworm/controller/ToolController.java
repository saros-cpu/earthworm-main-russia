package com.earthworm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ToolController {
    @GetMapping("/tool/dailySentence")
    public Map<String, Object> dailySentence() {
        return Map.of(
                "ru", "Повторение — мать учения.",
                "zh", "重复是学习之母。"
        );
    }
}
