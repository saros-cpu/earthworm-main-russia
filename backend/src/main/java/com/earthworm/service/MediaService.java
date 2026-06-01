package com.earthworm.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private final Path mediaRootPath;
    private final String ffmpegCommand;
    private final long cacheMaxBytes;
    private final ThreadPoolExecutor transcodeExecutor;
    private static final Path CACHE_DIR = Path.of("runtime", "transcode-cache");
    private final Set<String> transcodingInProgress = ConcurrentHashMap.newKeySet();

    private static final Map<String, MediaTypeInfo> MEDIA_TYPES = Map.ofEntries(
        Map.entry("mp4", new MediaTypeInfo("video/mp4", false)),
        Map.entry("webm", new MediaTypeInfo("video/webm", false)),
        Map.entry("avi", new MediaTypeInfo("video/x-msvideo", true)),
        Map.entry("flv", new MediaTypeInfo("video/x-flv", true)),
        Map.entry("wmv", new MediaTypeInfo("video/x-ms-wmv", true)),
        Map.entry("mp3", new MediaTypeInfo("audio/mpeg", false)),
        Map.entry("wma", new MediaTypeInfo("audio/x-ms-wma", true)),
        Map.entry("wav", new MediaTypeInfo("audio/wav", false)),
        Map.entry("ogg", new MediaTypeInfo("audio/ogg", false)),
        Map.entry("m4a", new MediaTypeInfo("audio/mp4", false))
    );

    public MediaService(
            @Value("${media.root-path}") String mediaRootPath,
            @Value("${media.ffmpeg-path}") String ffmpegPath,
            @Value("${media.cache-max-bytes:2147483648}") long cacheMaxBytes,
            @Value("${media.transcode-workers:2}") int transcodeWorkers,
            @Value("${media.transcode-queue-capacity:8}") int transcodeQueueCapacity) {
        this.mediaRootPath = Path.of(mediaRootPath).normalize();
        this.ffmpegCommand = ffmpegPath != null && !ffmpegPath.isBlank() ? ffmpegPath : "ffmpeg";
        this.cacheMaxBytes = cacheMaxBytes;
        this.transcodeExecutor = new ThreadPoolExecutor(
                transcodeWorkers, transcodeWorkers, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(transcodeQueueCapacity),
                runnable -> {
                    Thread thread = new Thread(runnable, "media-transcode");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy());
    }

    public Path getMediaRootPath() {
        return mediaRootPath;
    }

    public Path resolveFile(String relativePath) {
        try {
            String cleaned = relativePath.replace("\\", "/");
            Path file = mediaRootPath.resolve(cleaned).normalize();
            if (!file.startsWith(mediaRootPath)) return null;
            if (!Files.exists(file) || !Files.isRegularFile(file)) return null;
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public Path findCachedFromRelativePath(String relativePath) {
        try {
            String cleaned = relativePath.replace("\\", "/");
            Path file = mediaRootPath.resolve(cleaned).normalize();
            if (!file.startsWith(mediaRootPath)) return null;
            String filename = file.getFileName().toString();
            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";
            boolean isAudioFile = isAudio(ext);
            String targetExt = isAudioFile ? "mp3" : "mp4";
            String cacheKey = sanitizePath(file);
            Path cachedFile = CACHE_DIR.resolve(cacheKey + "." + targetExt);
            if (Files.exists(cachedFile) && Files.isRegularFile(cachedFile)) {
                return cachedFile;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public MediaTypeInfo getMediaInfo(String extension) {
        return MEDIA_TYPES.getOrDefault(extension.toLowerCase(),
                new MediaTypeInfo("application/octet-stream", false));
    }

    public Path getCachedFile(Path originalFile, String extension) {
        boolean isAudioFile = isAudio(extension);
        String targetExt = isAudioFile ? "mp3" : "mp4";
        String cacheKey = sanitizePath(originalFile);
        Path cachedFile = CACHE_DIR.resolve(cacheKey + "." + targetExt);
        if (!Files.exists(cachedFile)) return null;
        // Check cache is not older than original
        try {
            long cachedAge = System.currentTimeMillis() - Files.getLastModifiedTime(cachedFile).toMillis();
            long originalAge = System.currentTimeMillis() - Files.getLastModifiedTime(originalFile).toMillis();
            if (cachedAge < originalAge) return cachedFile;
        } catch (IOException e) {
            return cachedFile;
        }
        return null;
    }

    public boolean startAsyncTranscoding(Path originalFile, String extension) {
        String key = sanitizePath(originalFile);
        if (!transcodingInProgress.add(key)) return true;
        try {
            transcodeExecutor.execute(() -> {
                try {
                    doTranscode(originalFile, extension);
                } finally {
                    transcodingInProgress.remove(key);
                }
            });
            return true;
        } catch (RejectedExecutionException exception) {
            transcodingInProgress.remove(key);
            log.warn("Transcoding queue is full for {}", originalFile.getFileName());
            return false;
        }
    }

    private void doTranscode(Path originalFile, String extension) {
        boolean isAudioFile = isAudio(extension);
        String targetExt = isAudioFile ? "mp3" : "mp4";
        String cacheKey = sanitizePath(originalFile);
        Path cachedFile = CACHE_DIR.resolve(cacheKey + "." + targetExt);
        Path tempFile = CACHE_DIR.resolve(cacheKey + ".tmp." + targetExt);

        try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}

        try {
            Files.createDirectories(CACHE_DIR);
            String[] cmd = isAudioFile
                ? new String[]{
                    ffmpegCommand, "-y",
                    "-i", originalFile.toString(),
                    "-c:a", "libmp3lame",
                    "-b:a", "128k",
                    "-q:a", "2",
                    tempFile.toString()
                }
                : new String[]{
                    ffmpegCommand, "-y",
                    "-i", originalFile.toString(),
                    "-c:v", "libx264",
                    "-preset", "fast",
                    "-crf", "23",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    tempFile.toString()
                };
            log.info("Transcoding {} -> {}: {}", originalFile.getFileName(), targetExt, String.join(" ", cmd));
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                log.warn("Transcoding timed out for {}", originalFile.getFileName());
                process.destroyForcibly();
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
                return;
            }
            if (process.exitValue() != 0) {
                log.error("Transcoding failed for {} (exit={})", originalFile.getFileName(), process.exitValue());
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
                return;
            }
            log.info("Transcoding succeeded for {} -> {}", originalFile.getFileName(), cachedFile.getFileName());
            Files.move(tempFile, cachedFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            trimCache();
        } catch (Exception e) {
            log.error("Transcoding error for {}: {}", originalFile.getFileName(), e.getMessage());
            try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
        }
    }

    private synchronized void trimCache() {
        try (var files = Files.list(CACHE_DIR)) {
            List<Path> cachedFiles = files.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().contains(".tmp."))
                    .sorted(Comparator.comparingLong(this::lastModified))
                    .toList();
            long total = 0;
            for (Path file : cachedFiles) {
                total += Files.size(file);
            }
            for (Path file : cachedFiles) {
                if (total <= cacheMaxBytes) {
                    break;
                }
                long size = Files.size(file);
                Files.deleteIfExists(file);
                total -= size;
            }
        } catch (IOException exception) {
            log.warn("Unable to trim transcode cache: {}", exception.getMessage());
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            return Long.MIN_VALUE;
        }
    }

    @PreDestroy
    public void shutdown() {
        transcodeExecutor.shutdownNow();
    }

    private boolean isAudio(String extension) {
        return Set.of("mp3", "wav", "wma", "ogg", "flac", "m4a", "aac").contains(extension.toLowerCase());
    }

    private String sanitizePath(Path file) {
        return file.toAbsolutePath().toString()
                .replace(':', '_')
                .replace('\\', '_')
                .replace('/', '_');
    }

    public record MediaTypeInfo(String mimeType, boolean needsTranscoding) {}
}
