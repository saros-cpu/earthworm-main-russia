package com.earthworm.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiRequestRateLimiterTest {

    @Test
    void requireAllowed_shouldRejectAdditionalRequestsWithinTheWindow() {
        AiRequestRateLimiter limiter = new AiRequestRateLimiter();

        assertDoesNotThrow(() -> limiter.requireAllowed("assistant", "user-1", 1));
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> limiter.requireAllowed("assistant", "user-1", 1)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        assertDoesNotThrow(() -> limiter.requireAllowed("assistant", "user-2", 1));
    }

    @Test
    void requireAllowed_shouldBoundTrackedAiRequestWindows() {
        AiRequestRateLimiter limiter = new AiRequestRateLimiter();
        ReflectionTestUtils.setField(limiter, "maxTrackedWindows", 2);

        assertDoesNotThrow(() -> limiter.requireAllowed("assistant", "user-1", 2));
        assertDoesNotThrow(() -> limiter.requireAllowed("assistant", "user-2", 2));
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> limiter.requireAllowed("assistant", "user-3", 2)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        assertEquals(2, limiter.trackedWindowCount());
        assertDoesNotThrow(() -> limiter.requireAllowed("assistant", "user-1", 2));
    }
}
