package com.earthworm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiRefinementClientTest {

    @Test
    void generateCourse_shouldFailInsteadOfCreatingPlaceholderContentWhenNoKeyConfigured() {
        OpenAiRefinementClient client = new OpenAiRefinementClient(
                new ObjectMapper(),
                Mockito.mock(AiRequestRateLimiter.class),
                Mockito.mock(AiUsageBudgetService.class)
        );
        ReflectionTestUtils.setField(client, "enabled", true);
        ReflectionTestUtils.setField(client, "apiKey", "");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> client.generateCourse("travel", "beginner", 10)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void generateCourse_shouldApplyRateLimitBeforeCallingProvider() {
        AiRequestRateLimiter rateLimiter = Mockito.mock(AiRequestRateLimiter.class);
        AiUsageBudgetService usageBudgetService = Mockito.mock(AiUsageBudgetService.class);
        OpenAiRefinementClient client = new OpenAiRefinementClient(new ObjectMapper(), rateLimiter, usageBudgetService);
        ReflectionTestUtils.setField(client, "enabled", true);
        ReflectionTestUtils.setField(client, "apiKey", "configured-for-test");
        ReflectionTestUtils.setField(client, "requestsPerMinute", 5);
        Mockito.doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS))
                .when(rateLimiter).requireAllowed("course-generation", "system", 5);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> client.generateCourse("travel", "beginner", 10)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        Mockito.verifyNoInteractions(usageBudgetService);
    }

    @Test
    void generateCourse_shouldReserveDailyBudgetBeforeCallingProvider() {
        AiUsageBudgetService usageBudgetService = Mockito.mock(AiUsageBudgetService.class);
        OpenAiRefinementClient client = new OpenAiRefinementClient(
                new ObjectMapper(),
                Mockito.mock(AiRequestRateLimiter.class),
                usageBudgetService
        );
        ReflectionTestUtils.setField(client, "enabled", true);
        ReflectionTestUtils.setField(client, "apiKey", "configured-for-test");
        ReflectionTestUtils.setField(client, "maxOutputTokens", 1200);
        Mockito.doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS))
                .when(usageBudgetService).reserve("course-generation", "system", 1200);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> client.generateCourse("travel", "beginner", 10)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        Mockito.verify(usageBudgetService).reserve("course-generation", "system", 1200);
    }

    @Test
    void readBounded_shouldRejectOversizedProviderResponseBeforeParsing() {
        OpenAiRefinementClient client = new OpenAiRefinementClient(
                new ObjectMapper(),
                Mockito.mock(AiRequestRateLimiter.class),
                Mockito.mock(AiUsageBudgetService.class)
        );
        ReflectionTestUtils.setField(client, "maxResponseChars", 10);

        assertThrows(
                IOException.class,
                () -> client.readBounded(new ByteArrayInputStream("x".repeat(11).getBytes(StandardCharsets.UTF_8)))
        );
    }
}
