package com.earthworm.service;

import com.earthworm.model.Statement;
import com.earthworm.repository.StatementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private StatementRepository statementRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private AiRequestRateLimiter rateLimiter;
    @Mock
    private AiUsageBudgetService usageBudgetService;

    private AiAssistantService service;

    @BeforeEach
    void setUp() {
        service = new AiAssistantService(statementRepository, courseService, rateLimiter, usageBudgetService, new ObjectMapper());
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "apiKey", "");
        ReflectionTestUtils.setField(service, "maxQuestionLength", 2000);
        ReflectionTestUtils.setField(service, "requestsPerMinute", 10);
        ReflectionTestUtils.setField(service, "maxOutputTokens", 1200);
    }

    @Test
    void ask_shouldRejectOversizedQuestionBeforeLoadingContext() {
        assertThrows(IllegalArgumentException.class, () -> service.ask("x".repeat(2001), "statement-1"));

        verifyNoInteractions(courseService, statementRepository);
    }

    @Test
    void ask_shouldRejectInaccessibleStatementBeforeLoadingItsText() {
        doThrow(new NoSuchElementException("Statement not found"))
                .when(courseService).requireAccessibleStatement("statement-private");

        assertThrows(
                NoSuchElementException.class,
                () -> service.ask("explain this", "statement-private")
        );

        verify(statementRepository, never()).findById(anyString());
    }

    @Test
    void ask_shouldLoadAuthorizedContextButReturnStableUnavailableAnswerWhenDisabled() {
        Statement statement = new Statement();
        statement.setId("statement-1");
        statement.setEnglish("Privet");
        statement.setChinese("Hello");
        when(statementRepository.findById("statement-1")).thenReturn(Optional.of(statement));

        Map<String, Object> response = service.ask("explain", "statement-1");

        verify(courseService).requireAccessibleStatement("statement-1");
        assertEquals("AI service is temporarily unavailable. Please try again later.", response.get("answer"));
    }

    @Test
    void ask_shouldApplyRateLimitBeforeRemoteRequest() {
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "configured-for-test");
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS))
                .when(rateLimiter).requireAllowed("assistant", "anonymous", 10);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.ask("explain", null)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        verifyNoInteractions(usageBudgetService);
    }

    @Test
    void ask_shouldReserveDailyBudgetBeforeRemoteRequest() {
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "apiKey", "configured-for-test");
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS))
                .when(usageBudgetService).reserve("assistant", "anonymous", 1200);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.ask("explain", null)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        verify(usageBudgetService).reserve("assistant", "anonymous", 1200);
    }
}
