package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseGenerationServiceTest {

    @Mock
    private CoursePackRepository coursePackRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private OpenAiRefinementClient openAiRefinementClient;
    @Mock
    private AdminCourseService adminCourseService;

    private CourseGenerationService service;
    private CoursePack pack;

    @BeforeEach
    void setUp() {
        service = new CourseGenerationService(
                coursePackRepository,
                courseRepository,
                statementRepository,
                openAiRefinementClient,
                adminCourseService
        );
        pack = new CoursePack();
        pack.setId("pack-1");
        when(coursePackRepository.findById("pack-1")).thenReturn(Optional.of(pack));
    }

    @Test
    void generateCourse_shouldRejectOversizedInputBeforeCallingAiOrSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateCourse("pack-1", Map.of("topic", "x".repeat(181)))
        );

        verifyNoInteractions(openAiRefinementClient);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void generateCourse_shouldRejectArchivedPackBeforeCallingAiOrSaving() {
        pack.setArchived(true);

        assertThrows(
                ResponseStatusException.class,
                () -> service.generateCourse("pack-1", Map.of("topic", "travel"))
        );

        verifyNoInteractions(openAiRefinementClient);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void generateCourse_shouldRejectOversizedAiContentBeforeSavingAnything() {
        when(openAiRefinementClient.generateCourse("travel", "beginner", 12)).thenReturn(Map.of(
                "title", "Travel",
                "items", List.of(Map.of("targetText", "x".repeat(4001), "translation", "translation"))
        ));

        assertThrows(
                ResponseStatusException.class,
                () -> service.generateCourse("pack-1", Map.of("topic", "travel"))
        );

        verify(courseRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void generateCourse_shouldSaveValidatedAiContent() {
        when(openAiRefinementClient.generateCourse("travel", "beginner", 1)).thenReturn(Map.of(
                "title", "Travel",
                "description", "Travel basics",
                "items", List.of(Map.of(
                        "targetText", "Privet",
                        "translation", "Hello",
                        "phonetic", "pri-vyet"
                ))
        ));
        when(courseRepository.findByCoursePackIdOrderByOrderAsc("pack-1")).thenReturn(List.of());
        when(adminCourseService.course(anyString())).thenReturn(Map.of("saved", true));

        service.generateCourse("pack-1", Map.of("topic", "travel", "count", 1));

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        ArgumentCaptor<Statement> statementCaptor = ArgumentCaptor.forClass(Statement.class);
        verify(courseRepository).save(courseCaptor.capture());
        verify(statementRepository).save(statementCaptor.capture());
        assertEquals("Travel", courseCaptor.getValue().getTitle());
        assertEquals("Privet", statementCaptor.getValue().getEnglish());
        assertEquals("Hello", statementCaptor.getValue().getChinese());
    }
}
