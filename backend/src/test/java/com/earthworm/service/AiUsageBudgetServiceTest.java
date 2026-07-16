package com.earthworm.service;

import com.earthworm.model.AiDailyBudget;
import com.earthworm.repository.AiDailyBudgetRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiUsageBudgetServiceTest {

    @Test
    void reserve_shouldPersistReservedOutputTokensForTheDay() {
        AiDailyBudgetRepository repository = mock(AiDailyBudgetRepository.class);
        AiUsageBudgetService service = new AiUsageBudgetService(repository);
        ReflectionTestUtils.setField(service, "dailyMaxReservedOutputTokens", 2000L);
        AiDailyBudget initialized = new AiDailyBudget();
        initialized.setUsageDate(LocalDate.now());
        when(repository.findByUsageDateForUpdate(LocalDate.now())).thenReturn(Optional.of(initialized));

        service.reserve("assistant", "user-1", 1200);

        ArgumentCaptor<AiDailyBudget> saved = ArgumentCaptor.forClass(AiDailyBudget.class);
        var persistenceOrder = inOrder(repository);
        persistenceOrder.verify(repository).ensureUsageDateExists(LocalDate.now());
        persistenceOrder.verify(repository).findByUsageDateForUpdate(LocalDate.now());
        persistenceOrder.verify(repository).save(saved.capture());
        assertEquals(1200L, saved.getValue().getReservedOutputTokens());
        assertEquals(1L, saved.getValue().getRequestCount());
    }

    @Test
    void reserve_shouldRejectRequestThatWouldExceedDailyBudget() {
        AiDailyBudgetRepository repository = mock(AiDailyBudgetRepository.class);
        AiUsageBudgetService service = new AiUsageBudgetService(repository);
        ReflectionTestUtils.setField(service, "dailyMaxReservedOutputTokens", 1500L);
        AiDailyBudget existing = new AiDailyBudget();
        existing.setUsageDate(LocalDate.now());
        existing.setReservedOutputTokens(1000L);
        when(repository.findByUsageDateForUpdate(LocalDate.now())).thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.reserve("assistant", "user-1", 600)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        verify(repository).ensureUsageDateExists(LocalDate.now());
        verify(repository, never()).save(existing);
    }
}
