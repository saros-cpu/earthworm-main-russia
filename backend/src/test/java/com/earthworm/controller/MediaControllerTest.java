package com.earthworm.controller;

import com.earthworm.service.CourseService;
import com.earthworm.service.MediaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaControllerTest {

    @Test
    void stream_shouldRejectOversizedPathBeforeCheckingCourseAccess() throws Exception {
        MediaService mediaService = mock(MediaService.class);
        CourseService courseService = mock(CourseService.class);
        MediaController controller = new MediaController(mediaService, courseService);

        var response = controller.stream("x".repeat(2049), new HttpHeaders());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(courseService, never()).canStreamMedia(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void stream_shouldReturnRangeNotSatisfiableForMalformedRange(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.writeString(tempDir.resolve("sample.mp4"), "sample media");
        MediaService mediaService = mock(MediaService.class);
        CourseService courseService = mock(CourseService.class);
        MediaController controller = new MediaController(mediaService, courseService);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RANGE, "bytes=invalid");
        when(courseService.canStreamMedia("sample.mp4")).thenReturn(true);
        when(mediaService.resolveFile("sample.mp4")).thenReturn(mediaFile);
        when(mediaService.getMediaInfo("mp4")).thenReturn(new MediaService.MediaTypeInfo("video/mp4", false));

        var response = controller.stream("sample.mp4", headers);

        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatusCode());
        assertEquals("bytes */12", response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
    }

    @Test
    void info_shouldNotReturnInternalCacheFilenameForTranscodedMedia(@TempDir Path tempDir) throws Exception {
        String mediaPath = "courses/lesson-1.avi";
        Path cachedFile = Files.writeString(tempDir.resolve("D__private_media_courses_lesson-1.avi.mp4"), "cached media");
        MediaService mediaService = mock(MediaService.class);
        CourseService courseService = mock(CourseService.class);
        MediaController controller = new MediaController(mediaService, courseService);
        when(courseService.canStreamMedia(mediaPath)).thenReturn(true);
        when(mediaService.findCachedFromRelativePath(mediaPath)).thenReturn(cachedFile);

        var response = controller.info(mediaPath);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("lesson-1.avi", response.getBody().get("filename"));
        assertFalse(response.getBody().get("filename").toString().contains("private"));
        assertEquals("private, no-store", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    void stream_shouldNotAllowSharedCachingOfAuthorizedMedia(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.writeString(tempDir.resolve("private.mp4"), "private media");
        MediaService mediaService = mock(MediaService.class);
        CourseService courseService = mock(CourseService.class);
        MediaController controller = new MediaController(mediaService, courseService);
        when(courseService.canStreamMedia("private.mp4")).thenReturn(true);
        when(mediaService.resolveFile("private.mp4")).thenReturn(mediaFile);
        when(mediaService.getMediaInfo("mp4")).thenReturn(new MediaService.MediaTypeInfo("video/mp4", false));

        var response = controller.stream("private.mp4", new HttpHeaders());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("private, no-store", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    void streamRange_shouldNotAllowSharedCachingOfAuthorizedMedia(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.writeString(tempDir.resolve("private.mp4"), "private media");
        MediaService mediaService = mock(MediaService.class);
        CourseService courseService = mock(CourseService.class);
        MediaController controller = new MediaController(mediaService, courseService);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RANGE, "bytes=0-2");
        when(courseService.canStreamMedia("private.mp4")).thenReturn(true);
        when(mediaService.resolveFile("private.mp4")).thenReturn(mediaFile);
        when(mediaService.getMediaInfo("mp4")).thenReturn(new MediaService.MediaTypeInfo("video/mp4", false));

        var response = controller.stream("private.mp4", headers);

        assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
        assertEquals("private, no-store", response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
    }
}
