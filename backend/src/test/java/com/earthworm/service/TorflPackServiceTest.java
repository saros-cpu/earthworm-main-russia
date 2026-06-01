package com.earthworm.service;

import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TorflPackServiceTest {

    @Mock
    private CoursePackRepository coursePackRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StatementRepository statementRepository;

    @Test
    void bootstrap_shouldNotReadOrWriteSeedContentByDefault() {
        TorflPackService service = new TorflPackService(
                coursePackRepository,
                courseRepository,
                statementRepository
        );

        service.bootstrap();

        verifyNoInteractions(coursePackRepository, courseRepository, statementRepository);
    }

    @Test
    void generate_shouldStillCreateSeedContentWhenExplicitlyRequested() {
        TorflPackService service = new TorflPackService(
                coursePackRepository,
                courseRepository,
                statementRepository
        );

        service.generate(Map.of("level", "A1"));

        verify(coursePackRepository).save(any(CoursePack.class));
        verify(courseRepository, atLeastOnce()).save(any(Course.class));
        verify(statementRepository, atLeastOnce()).save(any(Statement.class));
    }

    @Test
    void reseed_shouldRejectDestructiveContentReplacement() {
        TorflPackService service = new TorflPackService(
                coursePackRepository,
                courseRepository,
                statementRepository
        );

        assertThrows(ResponseStatusException.class, service::reseed);

        verifyNoInteractions(coursePackRepository, courseRepository, statementRepository);
    }
}
