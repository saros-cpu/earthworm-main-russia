package com.earthworm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicRequestRateLimiterTest {

    private PublicRequestRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new PublicRequestRateLimiter();
        ReflectionTestUtils.setField(limiter, "maxTrackedWindows", 2);
    }

    @Test
    void allow_shouldRejectAdditionalRequestsInTheSameWindow() {
        assertTrue(limiter.allow("auth-register", "198.51.100.1", 1));
        assertFalse(limiter.allow("auth-register", "198.51.100.1", 1));
    }

    @Test
    void allow_shouldBoundTrackingForRandomPublicSources() {
        assertTrue(limiter.allow("tts", "198.51.100.1", 2));
        assertTrue(limiter.allow("tts", "198.51.100.2", 2));
        assertFalse(limiter.allow("tts", "198.51.100.3", 2));

        assertEquals(2, limiter.trackedWindowCount());
        assertTrue(limiter.allow("tts", "198.51.100.1", 2));
    }
}
