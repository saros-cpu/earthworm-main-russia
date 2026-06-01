package com.earthworm.controller;

import com.earthworm.service.AdminCourseService;
import com.earthworm.service.AdminAuditService;
import com.earthworm.service.CourseGenerationService;
import com.earthworm.service.CourseTopicSearchService;
import com.earthworm.service.CustomCoursePackService;
import com.earthworm.service.TorflPackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/admin")
public class AdminCourseController {
    private final AdminCourseService adminCourseService;
    private final AdminAuditService adminAuditService;
    private final CourseGenerationService courseGenerationService;
    private final CourseTopicSearchService courseTopicSearchService;
    private final TorflPackService torflPackService;
    private final CustomCoursePackService customCoursePackService;

    public AdminCourseController(
            AdminCourseService adminCourseService,
            AdminAuditService adminAuditService,
            CourseGenerationService courseGenerationService,
            CourseTopicSearchService courseTopicSearchService,
            TorflPackService torflPackService,
            CustomCoursePackService customCoursePackService
    ) {
        this.adminCourseService = adminCourseService;
        this.adminAuditService = adminAuditService;
        this.courseGenerationService = courseGenerationService;
        this.courseTopicSearchService = courseTopicSearchService;
        this.torflPackService = torflPackService;
        this.customCoursePackService = customCoursePackService;
    }

    @PostMapping("/torfl-pack/generate")
    public Map<String, Object> generateTorflPack(@RequestBody Map<String, Object> body) {
        return audited("course-pack.torfl.generate", "course-pack", "torfl", () -> torflPackService.generate(body));
    }

    @PostMapping("/torfl-pack/reseed")
    public Map<String, Object> reseedTorflPacks() {
        adminAuditService.record("course-pack.torfl.reseed.blocked", "course-pack", "torfl");
        return Map.of(
                "disabled", true,
                "message", "Reseed is disabled until learning progress can be preserved.");
    }

    @PostMapping("/custom-pack/reseed")
    public Map<String, Object> reseedCustomPacks() {
        adminAuditService.record("course-pack.custom.reseed.blocked", "course-pack", "custom");
        return Map.of(
                "disabled", true,
                "message", "Reseed is disabled until learning progress can be preserved.");
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
        return audited(contentUpdateAction("course-pack", body), "course-pack", id,
                () -> adminCourseService.updateCoursePack(id, body));
    }

    @DeleteMapping("/course-packs/{id}")
    public Boolean deleteCoursePack(@PathVariable("id") String id) {
        return audited("course-pack.archive", "course-pack", id, () -> adminCourseService.deleteCoursePack(id));
    }

    @PostMapping("/course-packs/{id}/courses")
    public Map<String, Object> createCourse(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return audited("course.create", "course-pack", id, () -> adminCourseService.createCourse(id, body));
    }

    @PostMapping("/course-packs/{id}/generate-course")
    public Map<String, Object> generateCourse(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return audited("course.generate", "course-pack", id, () -> courseGenerationService.generateCourse(id, body));
    }

    @PostMapping("/vocabulary-course-pack")
    public Map<String, Object> generateVocabularyCoursePack(@RequestBody Map<String, Object> body) {
        throw vocabularyToolsUnavailable();
    }

    @PostMapping("/course-packs/{id}/refresh-vocabulary-prompts")
    public Map<String, Object> refreshVocabularyPrompts(@PathVariable("id") String id) {
        throw vocabularyToolsUnavailable();
    }

    @PostMapping("/course-packs/{id}/enrich-vocabulary")
    public Map<String, Object> enrichVocabulary(
            @PathVariable("id") String id,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        throw vocabularyToolsUnavailable();
    }

    @PostMapping("/course-packs/{id}/organize-vocabulary-courses")
    public Map<String, Object> organizeVocabularyCourses(@PathVariable("id") String id) {
        throw vocabularyToolsUnavailable();
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
        return audited(contentUpdateAction("course", body), "course", id,
                () -> adminCourseService.updateCourse(id, body));
    }

    @DeleteMapping("/courses/{id}")
    public Boolean deleteCourse(@PathVariable("id") String id) {
        return audited("course.archive", "course", id, () -> adminCourseService.deleteCourse(id));
    }

    @PostMapping("/courses/{id}/statements")
    public Map<String, Object> createStatement(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return audited("statement.create", "course", id, () -> adminCourseService.createStatement(id, body));
    }

    @PutMapping("/statements/{id}")
    public Map<String, Object> updateStatement(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        return audited(contentUpdateAction("statement", body), "statement", id,
                () -> adminCourseService.updateStatement(id, body));
    }

    @DeleteMapping("/statements/{id}")
    public Boolean deleteStatement(@PathVariable("id") String id) {
        return audited("statement.archive", "statement", id, () -> adminCourseService.deleteStatement(id));
    }

    @PostMapping("/statements/{id}/refine")
    public Map<String, Object> refineStatement(@PathVariable("id") String id) {
        return audited("statement.refine", "statement", id, () -> adminCourseService.refineStatement(id));
    }

    @PostMapping("/courses/{id}/refine-all")
    public Map<String, Object> refineAllStatements(@PathVariable("id") String id) {
        int count = adminCourseService.refineCourseStatements(id);
        adminAuditService.record("course.refine-all", "course", id);
        return Map.of("courseId", id, "refinedCount", count);
    }

    private ResponseStatusException vocabularyToolsUnavailable() {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Vocabulary pack tools are not available yet.");
    }

    private String contentUpdateAction(String targetType, Map<String, Object> body) {
        if (Boolean.TRUE.equals(body.get("archived"))) {
            return targetType + ".archive";
        }
        if (Boolean.FALSE.equals(body.get("archived"))) {
            return targetType + ".restore";
        }
        return targetType + ".update";
    }

    private <T> T audited(String action, String targetType, String targetId, Supplier<T> operation) {
        T result = operation.get();
        adminAuditService.record(action, targetType, targetId);
        return result;
    }
}
