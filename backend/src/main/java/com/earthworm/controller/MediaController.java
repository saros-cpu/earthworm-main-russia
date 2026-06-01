package com.earthworm.controller;

import com.earthworm.service.MediaService;
import com.earthworm.service.CourseService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/media")
public class MediaController {
    private static final int MAX_MEDIA_PATH_LENGTH = 2048;
    private static final String AUTHORIZED_MEDIA_CACHE_CONTROL = "private, no-store";

    private final MediaService mediaService;
    private final CourseService courseService;

    public MediaController(MediaService mediaService, CourseService courseService) {
        this.mediaService = mediaService;
        this.courseService = courseService;
    }

    @GetMapping("/stream")
    public ResponseEntity<Resource> stream(
            @RequestParam String path,
            @RequestHeader HttpHeaders headers) throws IOException {
        if (!validMediaPath(path)) {
            return ResponseEntity.badRequest().build();
        }
        if (!courseService.canStreamMedia(path)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Path file = mediaService.resolveFile(path);

        if (file == null) {
            Path cached = mediaService.findCachedFromRelativePath(path);
            if (cached != null) {
                String cacheName = cached.getFileName().toString();
                String cacheExt = cacheName.contains(".") ? cacheName.substring(cacheName.lastIndexOf('.') + 1) : "mp4";
                return serveFile(cached, cacheExt, headers, true);
            }
            return ResponseEntity.notFound().build();
        }

        String filename = file.getFileName().toString();
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";
        MediaService.MediaTypeInfo info = mediaService.getMediaInfo(ext);

        if (info.needsTranscoding()) {
            Path cached = mediaService.getCachedFile(file, ext);
            if (cached != null) {
                return serveFile(cached, ext, headers, true);
            }
            boolean accepted = mediaService.startAsyncTranscoding(file, ext);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.RETRY_AFTER, accepted ? "15" : "30")
                    .header("X-Transcoding", accepted ? "in-progress" : "queue-full")
                    .build();
        }
        return serveFile(file, ext, headers, false);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info(@RequestParam String path) {
        if (!validMediaPath(path)) {
            return ResponseEntity.badRequest().build();
        }
        if (!courseService.canStreamMedia(path)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Path file = mediaService.resolveFile(path);
        Path cached = null;
        if (file == null) {
            cached = mediaService.findCachedFromRelativePath(path);
            if (cached == null) {
                return ResponseEntity.notFound().build();
            }
        }

        try {
            Path target = file != null ? file : cached;
            String filename = file != null ? file.getFileName().toString() : displayFilename(path);
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";

            String contentType = file != null
                    ? mediaService.getMediaInfo(ext).mimeType()
                    : (target.getFileName().toString().endsWith(".mp3") ? "audio/mpeg" : "video/mp4");
            Map<String, Object> result = Map.of(
                    "filename", filename,
                    "size", Files.size(target),
                    "contentType", contentType,
                    "needsTranscoding", false,
                    "cachedOnly", file == null,
                    "lastModified", Files.getLastModifiedTime(target).toString()
            );
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, AUTHORIZED_MEDIA_CACHE_CONTROL)
                    .body(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> handleRangeRequest(
            Path file, long fileLength, String range, String contentType) throws IOException {
        long start;
        long end;
        try {
            String requested = range.substring("bytes=".length()).trim();
            if (requested.isEmpty() || requested.contains(",") || requested.indexOf('-') < 0 || fileLength <= 0) {
                return rangeNotSatisfiable(fileLength);
            }
            String[] ranges = requested.split("-", -1);
            if (ranges.length != 2) {
                return rangeNotSatisfiable(fileLength);
            }
            if (ranges[0].isEmpty()) {
                long suffixLength = Long.parseLong(ranges[1]);
                if (suffixLength <= 0) {
                    return rangeNotSatisfiable(fileLength);
                }
                start = Math.max(0, fileLength - suffixLength);
                end = fileLength - 1;
            } else {
                start = Long.parseLong(ranges[0]);
                end = ranges[1].isEmpty() ? fileLength - 1 : Long.parseLong(ranges[1]);
            }
        } catch (NumberFormatException exception) {
            return rangeNotSatisfiable(fileLength);
        }

        if (start < 0 || end < 0 || start >= fileLength || end >= fileLength || start > end) {
            return rangeNotSatisfiable(fileLength);
        }

        long contentLength = end - start + 1;
        InputStream inputStream = Files.newInputStream(file);
        inputStream.skipNBytes(start);

        InputStreamResource resource = new InputStreamResource(inputStream) {
            @Override
            public long contentLength() {
                return contentLength;
            }
        };

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                .header(HttpHeaders.CACHE_CONTROL, AUTHORIZED_MEDIA_CACHE_CONTROL)
                .body(resource);
    }

    private ResponseEntity<Resource> rangeNotSatisfiable(long fileLength) {
        return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                .build();
    }

    private ResponseEntity<Resource> serveFile(Path file, String ext, HttpHeaders headers, boolean isTranscoded) throws IOException {
        long fileLength = Files.size(file);
        String range = headers.getFirst(HttpHeaders.RANGE);
        String cachedExt = file.getFileName().toString();
        cachedExt = cachedExt.contains(".") ? cachedExt.substring(cachedExt.lastIndexOf('.') + 1) : ext;
        String contentType = isTranscoded
                ? ("mp3".equalsIgnoreCase(cachedExt) ? "audio/mpeg" : "video/mp4")
                : mediaService.getMediaInfo(ext).mimeType();

        if (range != null && range.startsWith("bytes=")) {
            return handleRangeRequest(file, fileLength, range, contentType);
        }

        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(fileLength)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CACHE_CONTROL, AUTHORIZED_MEDIA_CACHE_CONTROL)
                .body(resource);
    }

    private boolean validMediaPath(String path) {
        return path != null && !path.isBlank() && path.length() <= MAX_MEDIA_PATH_LENGTH;
    }

    private String displayFilename(String requestedPath) {
        String normalized = requestedPath.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        String filename = separator >= 0 ? normalized.substring(separator + 1) : normalized;
        return filename.isBlank() ? "media" : filename;
    }
}
