package com.earthworm.service;

import com.earthworm.model.AiDailyBudget;
import com.earthworm.repository.AiDailyBudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class AiUsageBudgetService {
    private static final Logger LOGGER = LoggerFactory.getLogger("AI_USAGE_AUDIT");
    private final AiDailyBudgetRepository repository;

    @Value("${openai.daily-max-reserved-output-tokens:100000}")
    private long dailyMaxReservedOutputTokens = 100_000L;

    public AiUsageBudgetService(AiDailyBudgetRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void reserve(String feature, String requesterId, int requestedOutputTokens) {
        long requested = Math.max(1, requestedOutputTokens);
        long limit = Math.max(1, dailyMaxReservedOutputTokens);
        LocalDate today = LocalDate.now();
        repository.ensureUsageDateExists(today);
        AiDailyBudget budget = repository.findByUsageDateForUpdate(today)
                .orElseThrow(() -> new IllegalStateException("Daily AI budget row could not be initialized"));
        long reserved = budget.getReservedOutputTokens() == null ? 0L : budget.getReservedOutputTokens();
        if (requested > limit || reserved > limit - requested) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Daily AI usage budget has been exhausted. Try again later.");
        }
        budget.setReservedOutputTokens(reserved + requested);
        budget.setRequestCount((budget.getRequestCount() == null ? 0L : budget.getRequestCount()) + 1L);
        repository.save(budget);
        LOGGER.info(
                "ai_budget_reserved feature={} requesterId={} requestedOutputTokens={} reservedOutputTokens={}",
                safeValue(feature),
                safeValue(requesterId),
                requested,
                budget.getReservedOutputTokens()
        );
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9_.:-]", "_");
        return sanitized.length() <= 128 ? sanitized : sanitized.substring(0, 128);
    }
}
