package com.earthworm.service;

import com.earthworm.config.UserContext;
import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCourseServiceTest {

    @Mock
    private CoursePackRepository coursePackRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private CourseRefinementService courseRefinementService;

    private AdminCourseService service;

    @BeforeEach
    void setUp() {
        service = new AdminCourseService(coursePackRepository, courseRepository, statementRepository, courseRefinementService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void stats_shouldOnlyLoadPublicPacksForRegularCaller() {
        UserContext.setRole("USER");
        when(coursePackRepository.findByShareLevelIgnoreCaseAndArchivedFalseOrderByOrderAsc("public")).thenReturn(List.of());

        service.stats();

        verify(coursePackRepository).findByShareLevelIgnoreCaseAndArchivedFalseOrderByOrderAsc("public");
        verify(coursePackRepository, never()).findAll();
    }

    @Test
    void stats_shouldLoadAllPacksForAdmin() {
        UserContext.setRole("ADMIN");
        when(coursePackRepository.findAll()).thenReturn(List.of());

        service.stats();

        verify(coursePackRepository).findAll();
        verify(coursePackRepository, never()).findByShareLevelIgnoreCaseAndArchivedFalseOrderByOrderAsc("public");
    }

    @Test
    void updateCoursePack_shouldRejectInvalidShareLevelBeforeSaving() {
        CoursePack pack = new CoursePack();
        when(coursePackRepository.findById("pack-1")).thenReturn(java.util.Optional.of(pack));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCoursePack("pack-1", Map.of("shareLevel", "everyone"))
        );

        verify(coursePackRepository, never()).save(any());
    }

    @Test
    void updateCoursePack_shouldRejectOversizedTitleBeforeSaving() {
        CoursePack pack = new CoursePack();
        when(coursePackRepository.findById("pack-1")).thenReturn(java.util.Optional.of(pack));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCoursePack("pack-1", Map.of("title", "x".repeat(201)))
        );

        verify(coursePackRepository, never()).save(any());
    }

    @Test
    void updateCoursePack_shouldRestoreWithoutPublishingAnArchivedPack() {
        CoursePack pack = new CoursePack();
        pack.setArchived(true);
        pack.setShareLevel("private");
        when(coursePackRepository.findById("pack-1")).thenReturn(java.util.Optional.of(pack));

        service.updateCoursePack("pack-1", Map.of("archived", false));

        assertFalse(pack.getArchived());
        assertEquals("private", pack.getShareLevel());
        verify(coursePackRepository).save(pack);
    }

    @Test
    void deleteCoursePack_shouldArchiveAndMakePackPrivateWithoutPhysicallyRemovingContent() {
        CoursePack pack = new CoursePack();
        pack.setShareLevel("public");
        when(coursePackRepository.findById("pack-1")).thenReturn(java.util.Optional.of(pack));

        assertTrue(service.deleteCoursePack("pack-1"));

        assertTrue(pack.getArchived());
        assertEquals("private", pack.getShareLevel());
        verify(coursePackRepository).save(pack);
        verify(coursePackRepository, never()).deleteById(any());
    }

    @Test
    void deleteCourseAndStatement_shouldArchiveWithoutPhysicallyRemovingContent() {
        Course course = new Course();
        Statement statement = new Statement();
        when(courseRepository.findById("course-1")).thenReturn(java.util.Optional.of(course));
        when(statementRepository.findById("statement-1")).thenReturn(java.util.Optional.of(statement));

        assertTrue(service.deleteCourse("course-1"));
        assertTrue(service.deleteStatement("statement-1"));

        assertTrue(course.getArchived());
        assertTrue(statement.getArchived());
        verify(courseRepository).save(course);
        verify(statementRepository).save(statement);
        verify(courseRepository, never()).deleteById(any());
        verify(statementRepository, never()).deleteById(any());
    }

    @Test
    void course_shouldIncludeStoredRefinementFieldsForEditing() {
        Course course = new Course();
        course.setId("course-1");
        Statement statement = statement("statement-1");
        when(courseRepository.findById("course-1")).thenReturn(java.util.Optional.of(course));
        when(statementRepository.findByCourseIdOrderByOrderAsc("course-1")).thenReturn(List.of(statement));
        when(courseRefinementService.findRefinements(List.of("statement-1")))
                .thenReturn(Map.of("statement-1", Map.of("grammarNote", "saved note", "refinementMode", "rules")));

        Map<String, Object> result = service.course("course-1");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> statements = (List<Map<String, Object>>) result.get("statements");

        assertEquals("saved note", statements.get(0).get("grammarNote"));
        assertEquals("rules", statements.get(0).get("refinementMode"));
    }

    @Test
    void updateStatement_shouldPersistManualRefinementFields() {
        Statement statement = statement("statement-1");
        when(statementRepository.findById("statement-1")).thenReturn(java.util.Optional.of(statement));
        when(courseRefinementService.findRefinements(List.of("statement-1"))).thenReturn(Map.of());

        Map<String, Object> result = service.updateStatement("statement-1", Map.of(
                "sourceText", "meaning",
                "targetText", "target",
                "phonetic", "sounds",
                "translation", "translation",
                "vocabulary", List.of(Map.of("word", "word", "meaning", "meaning")),
                "grammarNote", "grammar",
                "difficulty", "elementary"
        ));

        verify(courseRefinementService).upsertRefinement(
                eq("statement-1"), eq("meaning"), eq("target"), eq("translation"),
                anyList(), eq("grammar"), eq("elementary"));
        assertEquals("rules", result.get("refinementMode"));
    }

    @Test
    void refineStatement_shouldPersistRuleGeneratedRefinement() {
        Statement statement = statement("statement-1");
        Map<String, Object> refinement = Map.of(
                "translation", "meaning",
                "vocabulary", List.of(),
                "grammarNote", "grammar",
                "difficulty", "beginner",
                "refinementMode", "rules"
        );
        when(statementRepository.findById("statement-1")).thenReturn(java.util.Optional.of(statement));
        when(courseRefinementService.refineStatementWithRules(statement)).thenReturn(refinement);

        Map<String, Object> result = service.refineStatement("statement-1");

        verify(courseRefinementService).upsertRefinement(
                "statement-1", "meaning", "target", "meaning", List.of(), "grammar", "beginner");
        assertEquals("rules", result.get("refinementMode"));
    }

    private Statement statement(String id) {
        Statement statement = new Statement();
        statement.setId(id);
        statement.setChinese("meaning");
        statement.setEnglish("target");
        statement.setSoundmark("sounds");
        return statement;
    }
}
