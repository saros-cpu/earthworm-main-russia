package com.earthworm.controller;

import com.earthworm.service.AdminAuditService;
import com.earthworm.service.AdminCourseService;
import com.earthworm.service.CourseGenerationService;
import com.earthworm.service.CourseTopicSearchService;
import com.earthworm.service.CustomCoursePackService;
import com.earthworm.service.TorflPackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCourseControllerTest {
    @Mock
    private AdminCourseService adminCourseService;
    @Mock
    private AdminAuditService adminAuditService;
    @Mock
    private CourseGenerationService courseGenerationService;
    @Mock
    private CourseTopicSearchService courseTopicSearchService;
    @Mock
    private TorflPackService torflPackService;
    @Mock
    private CustomCoursePackService customCoursePackService;

    private AdminCourseController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminCourseController(
                adminCourseService,
                adminAuditService,
                courseGenerationService,
                courseTopicSearchService,
                torflPackService,
                customCoursePackService);
    }

    @Test
    void reseedTorfl_shouldAuditBlockedRequestWithoutInvokingService() {
        assertTrue((Boolean) controller.reseedTorflPacks().get("disabled"));

        verify(adminAuditService).record("course-pack.torfl.reseed.blocked", "course-pack", "torfl");
        verifyNoInteractions(torflPackService);
    }

    @Test
    void reseedCustom_shouldAuditBlockedRequestWithoutInvokingService() {
        assertTrue((Boolean) controller.reseedCustomPacks().get("disabled"));

        verify(adminAuditService).record("course-pack.custom.reseed.blocked", "course-pack", "custom");
        verifyNoInteractions(customCoursePackService);
    }

    @Test
    void deleteCoursePack_shouldArchiveAndAuditSuccessfulAction() {
        when(adminCourseService.deleteCoursePack("pack-1")).thenReturn(true);

        assertTrue(controller.deleteCoursePack("pack-1"));

        verify(adminCourseService).deleteCoursePack("pack-1");
        verify(adminAuditService).record("course-pack.archive", "course-pack", "pack-1");
    }

    @Test
    void deleteCourseAndStatement_shouldAuditArchivalActions() {
        when(adminCourseService.deleteCourse("course-1")).thenReturn(true);
        when(adminCourseService.deleteStatement("statement-1")).thenReturn(true);

        assertTrue(controller.deleteCourse("course-1"));
        assertTrue(controller.deleteStatement("statement-1"));

        verify(adminAuditService).record("course.archive", "course", "course-1");
        verify(adminAuditService).record("statement.archive", "statement", "statement-1");
    }

    @Test
    void updateEndpoints_shouldAuditArchiveAndRestoreStateChangesPrecisely() {
        when(adminCourseService.updateCoursePack("pack-1", Map.of("archived", false))).thenReturn(Map.of());
        when(adminCourseService.updateCourse("course-1", Map.of("archived", true))).thenReturn(Map.of());
        when(adminCourseService.updateStatement("statement-1", Map.of("sourceText", "updated"))).thenReturn(Map.of());

        controller.updateCoursePack("pack-1", Map.of("archived", false));
        controller.updateCourse("course-1", Map.of("archived", true));
        controller.updateStatement("statement-1", Map.of("sourceText", "updated"));

        verify(adminAuditService).record("course-pack.restore", "course-pack", "pack-1");
        verify(adminAuditService).record("course.archive", "course", "course-1");
        verify(adminAuditService).record("statement.update", "statement", "statement-1");
    }
}
