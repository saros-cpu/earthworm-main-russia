package com.earthworm.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class FfmpegHealthIndicator implements HealthIndicator {

    @Value("${media.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${media.health-cache-ms:60000}")
    private long healthCacheMs;

    @Value("${media.health-timeout-ms:3000}")
    private long healthTimeoutMs;

    private volatile Health cachedHealth;
    private volatile long cachedAtMs;

    @Override
    public Health health() {
        long now = System.currentTimeMillis();
        Health current = cachedHealth;
        if (current != null && now - cachedAtMs < healthCacheMs) {
            return current;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            current = cachedHealth;
            if (current != null && now - cachedAtMs < healthCacheMs) {
                return current;
            }
            Health checked = performHealthCheck();
            cachedHealth = checked;
            cachedAtMs = now;
            return checked;
        }
    }

    protected Health performHealthCheck() {
        Process process = null;
        try {
            process = new ProcessBuilder(ffmpegPath, "-version")
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(Math.max(1L, healthTimeoutMs), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return Health.down()
                        .withDetail("error", "ffmpeg health check timed out")
                        .build();
            }
            String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            int exitCode = process.exitValue();

            if (exitCode == 0) {
                String version = output.lines().findFirst().orElse("unknown");
                return Health.up()
                        .withDetail("version", version)
                        .build();
            }
            return Health.down()
                    .withDetail("error", "ffmpeg exited with code " + exitCode)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "ffmpeg health check failed")
                    .build();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
