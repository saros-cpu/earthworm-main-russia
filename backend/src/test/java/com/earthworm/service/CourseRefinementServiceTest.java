package com.earthworm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRefinementServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    private CourseRefinementService service;

    @BeforeEach
    void setUp() {
        service = new CourseRefinementService(jdbcTemplate, new ObjectMapper());
    }

    @Test
    void upsertRefinement_shouldSurfaceStorageFailure() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataAccessResourceFailureException("write failed"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.upsertRefinement(
                "statement-1", "source", "target", "translation",
                List.of(Map.of("word", "word")), "grammar", "beginner"));
    }

    @Test
    void upsertRefinement_shouldMarkOverwrittenRowsAsRules() {
        service.upsertRefinement(
                "statement-1", "source", "target", "translation",
                List.of(), "grammar", "beginner");

        verify(jdbcTemplate).update(
                contains("refinement_mode='rules'"), any(), any(), any(), any(), any(), any(), any());
    }
}
