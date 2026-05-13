package com.earthworm.controller;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class UserController {
    @GetMapping("/user")
    public Map<String, Object> currentUser() {
        Map<String, Object> membership = new LinkedHashMap<>();
        membership.put("isMember", false);
        membership.put("details", null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("membership", membership);
        return result;
    }

    @PatchMapping("/user")
    public Map<String, Object> updateUser(@RequestBody Map<String, Object> body) {
        return body;
    }

    @PostMapping("/user/setup")
    public Map<String, Object> setup(@RequestBody Map<String, Object> body) {
        return body;
    }
}
