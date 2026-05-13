package com.earthworm.controller;

import com.earthworm.service.MasteredElementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class MasteredElementController {
    private final MasteredElementService service;

    public MasteredElementController(MasteredElementService service) {
        this.service = service;
    }

    @GetMapping("/mastered-elements")
    public List<Map<String, Object>> all() {
        return service.findAll();
    }

    @PostMapping("/mastered-elements")
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        return service.add(body);
    }

    @DeleteMapping("/mastered-elements/{id}")
    public Boolean remove(@PathVariable("id") String id) {
        return service.remove(id);
    }
}
