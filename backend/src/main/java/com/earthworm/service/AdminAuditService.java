package com.earthworm.service;

import com.earthworm.config.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminAuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger("ADMIN_AUDIT");

    public void record(String action, String targetType, String targetId) {
        LOGGER.info(
                "admin_action action={} actorId={} targetType={} targetId={}",
                safeValue(action),
                safeValue(UserContext.getUserIdOptional().orElse("unknown")),
                safeValue(targetType),
                safeValue(targetId)
        );
    }

    String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9_.:-]", "_");
        return sanitized.length() <= 128 ? sanitized : sanitized.substring(0, 128);
    }
}
