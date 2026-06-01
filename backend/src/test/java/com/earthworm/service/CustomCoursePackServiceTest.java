package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomCoursePackServiceTest {

    @Mock
    private CoursePackRepository coursePackRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StatementRepository statementRepository;

    private CustomCoursePackService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = new CustomCoursePackService(coursePackRepository, courseRepository, statementRepository);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        CustomCoursePackService.LYRICS_CACHE.clear();
    }

    @Test
    void syncVideoPaths_shouldNotOverwriteExistingMediaPath() throws Exception {
        Course course = course("course-a", "admin-edited.mp4");
        when(courseRepository.findByCoursePackIdOrderByOrderAsc("pack-1")).thenReturn(List.of(course));
        JsonNode pack = objectMapper.readTree("""
                {"id":"pack-1","courses":[{"id":"course-a","video":"seed.mp4"}]}
                """);

        service.syncVideoPaths(pack);

        assertEquals("admin-edited.mp4", course.getVideo());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void syncVideoPaths_shouldFillMissingMediaByCourseIdRatherThanListPosition() throws Exception {
        Course courseB = course("course-b", "");
        Course courseA = course("course-a", "");
        when(courseRepository.findByCoursePackIdOrderByOrderAsc("pack-1"))
                .thenReturn(List.of(courseB, courseA));
        JsonNode pack = objectMapper.readTree("""
                {"id":"pack-1","courses":[
                  {"id":"course-a","video":"a.mp4"},
                  {"id":"course-b","video":"b.mp4"}
                ]}
                """);

        service.syncVideoPaths(pack);

        assertEquals("a.mp4", courseA.getVideo());
        assertEquals("b.mp4", courseB.getVideo());
        verify(courseRepository).save(courseA);
        verify(courseRepository).save(courseB);
    }

    @Test
    void bootstrap_shouldNotWriteSeedContentByDefault() {
        service.bootstrap();

        verify(coursePackRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
    }

    @Test
    void syncVideoPaths_shouldStillLoadLyricsWhenStartupWritesAreDisabled() throws Exception {
        Course course = course("course-a", "");
        when(courseRepository.findByCoursePackIdOrderByOrderAsc("pack-1")).thenReturn(List.of(course));
        JsonNode pack = objectMapper.readTree("""
                {"id":"pack-1","courses":[{"id":"course-a","video":"seed.mp3","lyrics":[{"text":"line"}]}]}
                """);

        service.syncVideoPaths(pack, false);

        assertEquals("", course.getVideo());
        assertEquals("[{\"text\":\"line\"}]", CustomCoursePackService.LYRICS_CACHE.get("course-a"));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void reseed_shouldRejectDestructiveContentReplacement() {
        assertThrows(ResponseStatusException.class, service::reseed);

        verify(coursePackRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
        verify(statementRepository, never()).save(any());
        verify(coursePackRepository, never()).delete(any());
        verify(courseRepository, never()).delete(any());
        verify(statementRepository, never()).delete(any());
    }

    private Course course(String id, String video) {
        Course course = new Course();
        course.setId(id);
        course.setVideo(video);
        return course;
    }
}
