package com.earthworm.controller;

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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class TextToSpeechController {
    private static final Path CACHE_DIR = Path.of("runtime", "tts-cache");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @GetMapping(value = "/tts/ru", produces = "audio/mpeg")
    public ResponseEntity<byte[]> russianTextToSpeech(@RequestParam("text") String text)
            throws IOException, InterruptedException {
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedText = text.trim();
        Path cachePath = cachePath(normalizedText);
        if (Files.exists(cachePath)) {
            return audioResponse(Files.readAllBytes(cachePath));
        }

        List<String> segments = splitText(normalizedText);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String segment : segments) {
            outputStream.write(fetchAudio(segment));
        }

        byte[] audio = outputStream.toByteArray();
        Files.createDirectories(CACHE_DIR);
        Files.write(cachePath, audio);

        return audioResponse(audio);
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

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        String contentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse("");
        if (response.statusCode() != 200 || !contentType.toLowerCase().startsWith("audio/")) {
            throw new IOException("Russian TTS service temporarily unavailable");
        }
        return response.body();
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
