package com.earthworm.controller;

import com.earthworm.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserCourseProgressController {
    private final CourseService courseService;

    public UserCourseProgressController(CourseService courseService) {
        this.courseService = courseService;
    }

    public static class ProgressRequest {
        private String coursePackId;
        private String courseId;
        private Integer statementIndex;

        public String getCoursePackId() {
            return coursePackId;
        }

        public void setCoursePackId(String coursePackId) {
            this.coursePackId = coursePackId;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public Integer getStatementIndex() {
            return statementIndex;
        }

        public void setStatementIndex(Integer statementIndex) {
            this.statementIndex = statementIndex;
        }
    }

    @PutMapping("/user-course-progress")
    public Map<String, Object> updateProgress(@RequestBody ProgressRequest request) {
        return courseService.upsertProgress(
                request.getCoursePackId(),
                request.getCourseId(),
                request.getStatementIndex()
        );
    }

    @GetMapping("/user-course-progress/recent-course-packs")
    public List<Map<String, Object>> recentCoursePacks() {
        return courseService.recentCoursePacks();
    }
}
