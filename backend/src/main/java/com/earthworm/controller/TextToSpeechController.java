package com.earthworm.controller;

import com.earthworm.service.PublicRequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class TextToSpeechController {
    private static final Path CACHE_DIR = Path.of("runtime", "tts-cache");
    private final PublicRequestRateLimiter publicRequestRateLimiter;

    @Value("${tts.max-text-length:500}")
    private int maxTextLength;

    @Value("${tts.requests-per-minute:20}")
    private int requestsPerMinute;

    @Value("${tts.cache-max-bytes:268435456}")
    private long cacheMaxBytes;

    @Value("${tts.max-audio-bytes:10485760}")
    private long maxAudioBytes;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public TextToSpeechController(PublicRequestRateLimiter publicRequestRateLimiter) {
        this.publicRequestRateLimiter = publicRequestRateLimiter;
    }

    @GetMapping(value = "/tts/ru", produces = "audio/mpeg")
    public ResponseEntity<byte[]> russianTextToSpeech(
            @RequestParam("text") String text, HttpServletRequest request)
            throws IOException, InterruptedException {
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedText = text.trim();
        if (normalizedText.length() > maxTextLength) {
            return ResponseEntity.badRequest().build();
        }
        if (!publicRequestRateLimiter.allow("tts", request.getRemoteAddr(), requestsPerMinute)) {
            return ResponseEntity.status(429).build();
        }
        Path cachePath = cachePath(normalizedText);
        if (Files.exists(cachePath)) {
            if (Files.size(cachePath) > allowedAudioBytes()) {
                return ResponseEntity.status(503).build();
            }
            return audioResponse(Files.readAllBytes(cachePath));
        }
        List<String> segments = splitText(normalizedText);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String segment : segments) {
            byte[] segmentAudio = fetchAudio(segment);
            if ((long) outputStream.size() + segmentAudio.length > allowedAudioBytes()) {
                throw new IOException("Russian TTS response exceeds configured size limit");
            }
            outputStream.write(segmentAudio);
        }

        byte[] audio = outputStream.toByteArray();
        Files.createDirectories(CACHE_DIR);
        Files.write(cachePath, audio);
        trimCache();

        return audioResponse(audio);
    }

    private synchronized void trimCache() {
        try (var paths = Files.list(CACHE_DIR)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                    .sorted(Comparator.comparingLong(this::lastModified))
                    .toList();
            long total = 0;
            for (Path file : files) {
                total += Files.size(file);
            }
            for (Path file : files) {
                if (total <= cacheMaxBytes) {
                    break;
                }
                long size = Files.size(file);
                Files.deleteIfExists(file);
                total -= size;
            }
        } catch (IOException ignored) {
            // Cache cleanup failure should not prevent playback.
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return Long.MIN_VALUE;
        }
    }

    private ResponseEntity<byte[]> audioResponse(byte[] audio) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(audio);
    }

    private byte[] fetchAudio(String text) throws IOException, InterruptedException {
        String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=ru&q="
                + URLEncoder.encode(text, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "audio/mpeg,audio/*;q=0.9,*/*;q=0.8")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        String contentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse("");
        try (InputStream inputStream = response.body()) {
            if (response.statusCode() != 200 || !contentType.toLowerCase().startsWith("audio/")) {
                throw new IOException("Russian TTS service temporarily unavailable");
            }
            return readBoundedAudio(inputStream);
        }
    }

    byte[] readBoundedAudio(InputStream inputStream) throws IOException {
        int maximum = allowedAudioBytes();
        ByteArrayOutputStream audio = new ByteArrayOutputStream(Math.min(8192, maximum));
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            if ((long) audio.size() + read > maximum) {
                throw new IOException("Russian TTS response exceeds configured size limit");
            }
            audio.write(buffer, 0, read);
        }
        return audio.toByteArray();
    }

    private int allowedAudioBytes() {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, maxAudioBytes));
    }

    private Path cachePath(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder filename = new StringBuilder();
            for (byte item : hash) {
                filename.append(String.format("%02x", item));
            }
            return CACHE_DIR.resolve(filename + ".mp3");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private List<String> splitText(String text) {
        int maxLength = 120;
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : text.split("\\s+")) {
            if (current.length() > 0 && current.length() + word.length() + 1 > maxLength) {
                result.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(word);
        }

        if (!current.isEmpty()) {
            result.add(current.toString());
        }
        return result;
    }
}
