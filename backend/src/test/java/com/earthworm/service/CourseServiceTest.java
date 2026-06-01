package com.earthworm.service;

import com.earthworm.config.CurrentUser;
import com.earthworm.config.UserContext;
import com.earthworm.model.Course;
import com.earthworm.model.CoursePack;
import com.earthworm.model.Statement;
import com.earthworm.repository.CourseHistoryRepository;
import com.earthworm.repository.CoursePackRepository;
import com.earthworm.repository.CourseRepository;
import com.earthworm.repository.StatementRepository;
import com.earthworm.repository.UserCourseProgressRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CurrentUser currentUser;
    @Mock
    private CoursePackRepository coursePackRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private UserCourseProgressRepository progressRepository;
    @Mock
    private CourseHistoryRepository historyRepository;
    @Mock
    private CourseRefinementService refinementService;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(
                currentUser,
                coursePackRepository,
                courseRepository,
                statementRepository,
                progressRepository,
                historyRepository,
                refinementService
        );
        UserContext.setUserId("user-1");
        UserContext.setRole("USER");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void requireAccessibleStatement_shouldRejectStatementFromAnotherUsersPrivatePack() {
        stubStatementInPack("statement-1", "course-1", privatePack("pack-1", "user-2"));

        assertThrows(NoSuchElementException.class, () -> service.requireAccessibleStatement("statement-1"));
    }

    @Test
    void requireAccessibleStatement_shouldAllowStatementFromPublicPack() {
        stubStatementInPack("statement-1", "course-1", publicPack("pack-1"));

        assertDoesNotThrow(() -> service.requireAccessibleStatement("statement-1"));
    }

    @Test
    void requireAccessibleStatementSource_shouldRejectMismatchedPackReference() {
        stubStatementInPack("statement-1", "course-1", publicPack("pack-1"));

        assertThrows(
                NoSuchElementException.class,
                () -> service.requireAccessibleStatementSource("statement-1", "different-pack")
        );
    }

    @Test
    void canAccessStatement_shouldFilterStatementsFromPrivatePacksOwnedByOthers() {
        stubStatementInPack("statement-1", "course-1", privatePack("pack-1", "user-2"));

        assertFalse(service.canAccessStatement("statement-1"));
    }

    @Test
    void canAccessStatement_shouldKeepAccessibleStatements() {
        stubStatementInPack("statement-1", "course-1", publicPack("pack-1"));

        assertTrue(service.canAccessStatement("statement-1"));
    }

    @Test
    void accessibleStatementIds_shouldCheckAReviewListWithOneRepositoryQuery() {
        when(statementRepository.findAccessibleIds(List.of("statement-1", "statement-2"), "user-1", false))
                .thenReturn(List.of("statement-1"));

        assertEquals(Set.of("statement-1"), service.accessibleStatementIds(List.of("statement-1", "statement-2")));

        verify(statementRepository).findAccessibleIds(List.of("statement-1", "statement-2"), "user-1", false);
    }

    @Test
    void requireAccessibleStatement_shouldRejectArchivedStatement() {
        Statement archivedStatement = new Statement();
        archivedStatement.setId("statement-1");
        archivedStatement.setCourseId("course-1");
        archivedStatement.setArchived(true);
        when(statementRepository.findById("statement-1")).thenReturn(Optional.of(archivedStatement));

        assertThrows(NoSuchElementException.class, () -> service.requireAccessibleStatement("statement-1"));
    }

    @Test
    void requireAccessibleStatement_shouldRejectStatementFromArchivedPack() {
        CoursePack pack = publicPack("pack-1");
        pack.setArchived(true);
        stubStatementInPack("statement-1", "course-1", pack);

        assertThrows(NoSuchElementException.class, () -> service.requireAccessibleStatement("statement-1"));
    }

    private void stubStatementInPack(String statementId, String courseId, CoursePack pack) {
        Statement statement = new Statement();
        statement.setId(statementId);
        statement.setCourseId(courseId);
        Course course = new Course();
        course.setId(courseId);
        course.setCoursePackId(pack.getId());
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(coursePackRepository.findById(pack.getId())).thenReturn(Optional.of(pack));
    }

    private CoursePack privatePack(String id, String ownerId) {
        CoursePack pack = new CoursePack();
        pack.setId(id);
        pack.setShareLevel("private");
        pack.setCreatorId(ownerId);
        return pack;
    }

    private CoursePack publicPack(String id) {
        CoursePack pack = new CoursePack();
        pack.setId(id);
        pack.setShareLevel("public");
        pack.setCreatorId("system");
        return pack;
    }
}
