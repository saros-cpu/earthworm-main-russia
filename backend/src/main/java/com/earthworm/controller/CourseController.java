package com.earthworm.controller;

import com.earthworm.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/course-pack")
    public List<Map<String, Object>> coursePacks() {
        return courseService.findCoursePacks();
    }

    @GetMapping("/course-pack/{coursePackId}")
    public Map<String, Object> coursePack(@PathVariable("coursePackId") String coursePackId) {
        return courseService.findCoursePack(coursePackId);
    }

    @GetMapping("/course-pack/{coursePackId}/courses/{courseId}")
    public Map<String, Object> course(
            @PathVariable("coursePackId") String coursePackId,
            @PathVariable("courseId") String courseId
    ) {
        return courseService.findCourse(coursePackId, courseId);
    }

    @GetMapping("/course-pack/{coursePackId}/courses/{courseId}/next")
    public Map<String, Object> nextCourse(
            @PathVariable("coursePackId") String coursePackId,
            @PathVariable("courseId") String courseId
    ) {
        return courseService.findNextCourse(coursePackId, courseId);
    }

    @PostMapping("/course-pack/{coursePackId}/courses/{courseId}/complete")
    public Map<String, Object> completeCourse(
            @PathVariable("coursePackId") String coursePackId,
            @PathVariable("courseId") String courseId
    ) {
        return courseService.completeCourse(coursePackId, courseId);
    }
}
