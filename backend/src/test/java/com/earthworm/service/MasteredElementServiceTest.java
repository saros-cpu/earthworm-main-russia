package com.earthworm.service;

import com.earthworm.config.UserContext;
import com.earthworm.repository.MasteredElementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MasteredElementServiceTest {

    @Mock
    private MasteredElementRepository repository;

    private MasteredElementService service;

    @BeforeEach
    void setUp() {
        service = new MasteredElementService(repository, new ObjectMapper());
        UserContext.setUserId("user-1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void add_shouldRejectOversizedContentBeforeSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.add(Map.of("content", Map.of("targetText", "x".repeat(4001))))
        );

        verify(repository, never()).save(any());
    }
}
