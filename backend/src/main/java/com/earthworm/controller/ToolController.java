package com.earthworm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ToolController {
    @GetMapping("/tool/dailySentence")
    public Map<String, Object> dailySentence() {
        return Map.of(
                "en", "A journey of a thousand miles begins with a single step.",
                "zh", "千里之行，始于足下。"
        );
    }
}
