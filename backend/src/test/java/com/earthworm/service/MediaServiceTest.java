package com.earthworm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;

class MediaServiceTest {

    @Test
    void findCachedFromRelativePath_shouldNotUseCacheFromDifferentPathWithSameFilename(@TempDir Path mediaRoot)
            throws Exception {
        MediaService service = new MediaService(mediaRoot.toString(), "ffmpeg", 1024, 1, 1);
        String filename = "same-name-" + UUID.randomUUID() + ".avi";
        Path otherCourseFile = mediaRoot.resolve("private").resolve(filename);
        Path cacheDir = Path.of("runtime", "transcode-cache");
        String cachedName = otherCourseFile.toAbsolutePath().toString()
                .replace(':', '_')
                .replace('\\', '_')
                .replace('/', '_') + ".mp4";
        Path unrelatedCache = cacheDir.resolve(cachedName);
        Files.createDirectories(cacheDir);
        Files.writeString(unrelatedCache, "not the requested media");
        try {
            assertNull(service.findCachedFromRelativePath("public/" + filename));
        } finally {
            Files.deleteIfExists(unrelatedCache);
            service.shutdown();
        }
    }
}
