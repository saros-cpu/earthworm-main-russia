package com.earthworm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRequestRateLimiter {
    private static final long WINDOW_MILLIS = 60_000L;
    private final Map<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();

    @Value("${openai.max-tracked-rate-limit-windows:10000}")
    private int maxTrackedWindows = 10_000;

    public void requireAllowed(String feature, String requesterId, int requestsPerMinute) {
        String key = feature + ":" + requesterId;
        long now = System.currentTimeMillis();
        Deque<Long> window = requestWindows.get(key);
        if (window == null) {
            synchronized (requestWindows) {
                window = requestWindows.get(key);
                if (window == null) {
                    removeExpiredWindows(now);
                    if (requestWindows.size() >= Math.max(1, maxTrackedWindows)) {
                        throw rateLimited();
                    }
                    window = new ArrayDeque<>();
                    requestWindows.put(key, window);
                }
            }
        }
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() >= WINDOW_MILLIS) {
                window.removeFirst();
            }
            if (requestsPerMinute < 1 || window.size() >= requestsPerMinute) {
                throw rateLimited();
            }
            window.addLast(now);
        }
    }

    int trackedWindowCount() {
        return requestWindows.size();
    }

    private void removeExpiredWindows(long now) {
        requestWindows.forEach((key, window) -> {
            synchronized (window) {
                while (!window.isEmpty() && now - window.peekFirst() >= WINDOW_MILLIS) {
                    window.removeFirst();
                }
                if (window.isEmpty()) {
                    requestWindows.remove(key, window);
                }
            }
        });
    }

    private ResponseStatusException rateLimited() {
        return new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "AI request rate limit exceeded. Try again later.");
    }
}
