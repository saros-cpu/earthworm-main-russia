package com.earthworm.controller;

import com.earthworm.service.AdminCourseService;
import com.earthworm.service.CourseGenerationService;
import com.earthworm.service.CourseTopicSearchService;
import com.earthworm.service.CustomCoursePackService;
import com.earthworm.service.TorflPackService;
import com.earthworm.service.VocabularyCoursePackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminCourseController {
    private final AdminCourseService adminCourseService;
    private final CourseGenerationService courseGenerationService;
    private final CourseTopicSearchService courseTopicSearchService;
    private final VocabularyCoursePackService vocabularyCoursePackService;
    private final TorflPackService torflPackService;
    private final CustomCoursePackService customCoursePackService;

    public AdminCourseController(
            AdminCourseService adminCourseService,
            CourseGenerationService courseGenerationService,
            CourseTopicSearchService courseTopicSearchService,
            VocabularyCoursePackService vocabularyCoursePackService,
            TorflPackService torflPackService,
            CustomCoursePackService customCoursePackService
    ) {
        this.adminCourseService = adminCourseService;
        this.courseGenerationService = courseGenerationService;
        this.courseTopicSearchService = courseTopicSearchService;
        this.vocabularyCoursePackService = vocabularyCoursePackService;
        this.torflPackService = torflPackService;
        this.customCoursePackService = customCoursePackService;
    }

    @PostMapping("/torfl-pack/generate")
    public Map<String, Object> generateTorflPack(@RequestBody Map<String, Object> body) {
        return torflPackService.generate(body);
    }

    @PostMapping("/torfl-pack/reseed")
    public Map<String, Object> reseedTorflPacks() {
        return torflPackService.reseed();
    }

    @PostMapping("/custom-pack/reseed")
    public Map<String, Object> reseedCustomPacks() {
        return customCoursePackService.reseed();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return adminCourseService.stats();
    }

    @GetMapping("/course-packs")
    public List<Map<String, Object>> coursePacks() {
        return adminCourseService.coursePacks();
    }

    @GetMapping("/course-packs/{id}")
    public Map<String, Object> coursePack(@PathVariable("id") String id) {
        return adminCourseService.coursePack(id);
    }

    @PutMapping("/course-packs/{id}")
    public Map<String, Object> updateCoursePack(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return adminCourseService.updateCoursePack(id, body);
    }

    @DeleteMapping("/course-packs/{id}")
    public Boolean deleteCoursePack(@PathVariable("id") String id) {
        return adminCourseService.deleteCoursePack(id);
    }

    @PostMapping("/course-packs/{id}/courses")
    public Map<String, Object> createCourse(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return adminCourseService.createCourse(id, body);
    }

    @PostMapping("/course-packs/{id}/generate-course")
    public Map<String, Object> generateCourse(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return courseGenerationService.generateCourse(id, body);
    }

    @PostMapping("/vocabulary-course-pack")
    public Map<String, Object> generateVocabularyCoursePack(@RequestBody Map<String, Object> body) {
        return vocabularyCoursePackService.generate(body);
    }

    @PostMapping("/course-packs/{id}/refresh-vocabulary-prompts")
    public Map<String, Object> refreshVocabularyPrompts(@PathVariable("id") String id) {
        return vocabularyCoursePackService.refreshPrompts(id);
    }

    @PostMapping("/course-packs/{id}/enrich-vocabulary")
    public Map<String, Object> enrichVocabulary(
            @PathVariable("id") String id,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        return vocabularyCoursePackService.enrichVocabulary(id, body == null ? Map.of() : body);
    }

    @PostMapping("/course-packs/{id}/organize-vocabulary-courses")
    public Map<String, Object> organizeVocabularyCourses(@PathVariable("id") String id) {
        return vocabularyCoursePackService.organizeCourses(id);
    }

    @GetMapping("/course-topic-suggestions")
    public List<Map<String, Object>> courseTopicSuggestions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "online", defaultValue = "false") boolean online
    ) {
        if (online) {
            return courseTopicSearchService.searchOnline(keyword);
        }
        return courseTopicSearchService.search(keyword);
    }

    @GetMapping("/courses/{id}")
    public Map<String, Object> course(@PathVariable("id") String id) {
        return adminCourseService.course(id);
    }

    @PutMapping("/courses/{id}")
    public Map<String, Object> updateCourse(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return adminCourseService.updateCourse(id, body);
    }

    @DeleteMapping("/courses/{id}")
    public Boolean deleteCourse(@PathVariable("id") String id) {
        return adminCourseService.deleteCourse(id);
    }

    @PostMapping("/courses/{id}/statements")
    public Map<String, Object> createStatement(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return adminCourseService.createStatement(id, body);
    }

    @PutMapping("/statements/{id}")
    public Map<String, Object> updateStatement(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return adminCourseService.updateStatement(id, body);
    }

    @DeleteMapping("/statements/{id}")
    public Boolean deleteStatement(@PathVariable("id") String id) {
        return adminCourseService.deleteStatement(id);
    }

    @PostMapping("/statements/{id}/refine")
    public Map<String, Object> refineStatement(@PathVariable("id") String id) {
        return adminCourseService.refineStatement(id);
    }

    @PostMapping("/courses/{id}/refine-all")
    public Map<String, Object> refineAllStatements(@PathVariable("id") String id) {
        int count = adminCourseService.refineCourseStatements(id);
        return Map.of("courseId", id, "refinedCount", count);
    }
}
