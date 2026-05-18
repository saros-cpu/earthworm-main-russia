package com.earthworm.controller;

import com.earthworm.service.MediaService;
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

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/stream")
    public ResponseEntity<Resource> stream(
            @RequestParam String path,
            @RequestHeader HttpHeaders headers) throws IOException {
        Path file = mediaService.resolveFile(path);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = file.getFileName().toString();
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";
        MediaService.MediaTypeInfo info = mediaService.getMediaInfo(ext);

        if (info.needsTranscoding()) {
            Path cached = mediaService.getCachedFile(file, ext);
            if (cached != null) {
                // Serve cached mp4
                return serveFile(cached, ext, headers, true);
            }
            // No cache yet — start async transcoding, tell client to retry
            mediaService.startAsyncTranscoding(file, ext);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.RETRY_AFTER, "15")
                    .header("X-Transcoding", "in-progress")
                    .build();
        }
        return serveFile(file, ext, headers, false);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info(@RequestParam String path) {
        Path file = mediaService.resolveFile(path);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String filename = file.getFileName().toString();
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";
            MediaService.MediaTypeInfo info = mediaService.getMediaInfo(ext);

            Map<String, Object> result = Map.of(
                    "filename", filename,
                    "size", Files.size(file),
                    "contentType", info.mimeType(),
                    "needsTranscoding", info.needsTranscoding(),
                    "lastModified", Files.getLastModifiedTime(file).toString()
            );
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> handleRangeRequest(
            Path file, long fileLength, String range, String contentType) throws IOException {
        String[] ranges = range.substring("bytes=".length()).split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty()
                ? Long.parseLong(ranges[1])
                : fileLength - 1;

        if (start >= fileLength || end >= fileLength || start > end) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
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
                .body(resource);
    }

    private ResponseEntity<Resource> serveFile(Path file, String ext, HttpHeaders headers, boolean isTranscoded) throws IOException {
        long fileLength = Files.size(file);
        String range = headers.getFirst(HttpHeaders.RANGE);
        String contentType = isTranscoded
                ? (isAudio(ext) ? "audio/mpeg" : "video/mp4")
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
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(resource);
    }

    private boolean isAudio(String extension) {
        return "wma".equalsIgnoreCase(extension);
    }
}
