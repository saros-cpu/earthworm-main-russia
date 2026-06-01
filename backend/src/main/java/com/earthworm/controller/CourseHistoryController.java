package com.earthworm.controller;

import com.earthworm.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CourseHistoryController {
    private final CourseService courseService;

    public CourseHistoryController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/course-history/{coursePackId}")
    public List<Map<String, Object>> courseHistory(@PathVariable("coursePackId") String coursePackId) {
        return courseService.courseHistory(coursePackId);
    }
}
