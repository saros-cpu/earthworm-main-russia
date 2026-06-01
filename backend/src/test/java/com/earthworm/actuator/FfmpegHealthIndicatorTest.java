package com.earthworm.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FfmpegHealthIndicatorTest {

    @Test
    void health_shouldReuseCachedProbeForRepeatedPublicChecks() {
        CountingIndicator indicator = new CountingIndicator();
        ReflectionTestUtils.setField(indicator, "healthCacheMs", 60_000L);

        Health first = indicator.health();
        Health second = indicator.health();

        assertSame(first, second);
        assertEquals(1, indicator.checkCount);
    }

    @Test
    void performHealthCheck_shouldNotExposeExecutablePathOnFailure() {
        FfmpegHealthIndicator indicator = new FfmpegHealthIndicator();
        ReflectionTestUtils.setField(indicator, "ffmpegPath", "Z:\\private\\missing-ffmpeg.exe");
        ReflectionTestUtils.setField(indicator, "healthTimeoutMs", 100L);

        Health health = indicator.performHealthCheck();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("ffmpeg health check failed", health.getDetails().get("error"));
    }

    private static class CountingIndicator extends FfmpegHealthIndicator {
        private int checkCount;

        @Override
        protected Health performHealthCheck() {
            checkCount++;
            return Health.up().build();
        }
    }
}
