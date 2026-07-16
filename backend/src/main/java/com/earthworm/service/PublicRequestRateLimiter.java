package com.earthworm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PublicRequestRateLimiter {
    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();

    @Value("${public-rate-limit.max-tracked-windows:10000}")
    private int maxTrackedWindows = 10_000;

    public boolean allow(String feature, String clientId, int requestsPerMinute) {
        if (requestsPerMinute < 1) {
            return false;
        }
        String key = feature + ":" + normalizeClientId(clientId);
        long now = System.currentTimeMillis();
        Deque<Long> window = requestWindows.get(key);
        if (window == null) {
            synchronized (requestWindows) {
                window = requestWindows.get(key);
                if (window == null) {
                    removeExpiredWindows(now);
                    if (requestWindows.size() >= Math.max(1, maxTrackedWindows)) {
                        return false;
                    }
                    window = new ArrayDeque<>();
                    requestWindows.put(key, window);
                }
            }
        }
        synchronized (window) {
            removeExpiredRequests(window, now);
            if (window.size() >= requestsPerMinute) {
                return false;
            }
            window.addLast(now);
            return true;
        }
    }

    int trackedWindowCount() {
        return requestWindows.size();
    }

    private void removeExpiredWindows(long now) {
        requestWindows.forEach((key, window) -> {
            synchronized (window) {
                removeExpiredRequests(window, now);
                if (window.isEmpty()) {
                    requestWindows.remove(key, window);
                }
            }
        });
    }

    private void removeExpiredRequests(Deque<Long> window, long now) {
        while (!window.isEmpty() && now - window.peekFirst() >= WINDOW_MILLIS) {
            window.removeFirst();
        }
    }

    private String normalizeClientId(String clientId) {
        return clientId == null || clientId.isBlank() ? "unknown" : clientId;
    }
}
