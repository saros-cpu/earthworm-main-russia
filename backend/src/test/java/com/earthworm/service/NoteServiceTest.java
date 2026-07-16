package com.earthworm.service;

import com.earthworm.config.UserContext;
import com.earthworm.repository.StatementNoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private StatementNoteRepository repository;

    private NoteService service;

    @BeforeEach
    void setUp() {
        service = new NoteService(repository);
        UserContext.setUserId("user-1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void upsertNote_shouldRejectOversizedContentBeforeReadingOrWriting() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.upsertNote("statement-1", "x".repeat(4001))
        );

        verify(repository, never()).findByUserIdAndStatementId("user-1", "statement-1");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
