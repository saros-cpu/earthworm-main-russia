package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.service.StudyGroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
public class StudyGroupController {
    private final StudyGroupService service;

    public StudyGroupController(StudyGroupService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return service.listGroups();
    }

    @GetMapping("/my")
    public List<Map<String, Object>> myGroups() {
        return service.getUserGroups(UserContext.getUserId());
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        return service.createGroup(
                UserContext.getUserId(),
                (String) body.get("name"),
                (String) body.get("description")
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable("id") String id) {
        return service.getGroup(id, UserContext.getUserId());
    }

    @PostMapping("/{id}/join")
    public Map<String, Object> join(@PathVariable("id") String id) {
        return service.joinGroup(id, UserContext.getUserId());
    }

    @PostMapping("/{id}/leave")
    public Map<String, Object> leave(@PathVariable("id") String id) {
        return service.leaveGroup(id, UserContext.getUserId());
    }

    @GetMapping("/{id}/members")
    public List<Map<String, Object>> members(@PathVariable("id") String id) {
        return service.getGroupMembers(id);
    }

    @GetMapping("/{id}/activities")
    public List<Map<String, Object>> activities(@PathVariable("id") String id) {
        return service.getGroupActivities(id);
    }
}