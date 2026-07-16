package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.service.AiAssistantService;
import com.earthworm.service.CourseService;
import com.earthworm.service.DailyTaskService;
import com.earthworm.service.LearningStatsService;
import com.earthworm.service.NoteService;
import com.earthworm.service.ReviewService;
import com.earthworm.service.VocabularyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningControllerTest {

    @Mock
    private LearningStatsService statsService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private VocabularyService vocabularyService;
    @Mock
    private DailyTaskService taskService;
    @Mock
    private AiAssistantService aiService;
    @Mock
    private NoteService noteService;
    @Mock
    private CourseService courseService;

    private LearningController controller;

    @BeforeEach
    void setUp() {
        controller = new LearningController(
                statsService,
                reviewService,
                vocabularyService,
                taskService,
                aiService,
                noteService,
                courseService
        );
        UserContext.setUserId("user-1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void scheduleReview_shouldValidateStatementAccessBeforePersisting() {
        Map<String, Object> body = Map.of(
                "coursePackId", "pack-1",
                "courseId", "course-1",
                "statementId", "statement-1"
        );
        when(reviewService.scheduleReview("user-1", "statement-1", "pack-1", "course-1"))
                .thenReturn(Map.of("scheduled", true));
        when(reviewService.getDueReviews("user-1")).thenReturn(List.of());

        controller.scheduleReview(body);

        InOrder order = inOrder(courseService, reviewService);
        order.verify(courseService).requireAccessibleStatement("pack-1", "course-1", "statement-1");
        order.verify(reviewService).scheduleReview("user-1", "statement-1", "pack-1", "course-1");
    }

    @Test
    void recordReview_shouldValidateCurrentStatementAccessBeforeUpdatingSchedule() {
        when(reviewService.recordReview("user-1", "statement-1", 4))
                .thenReturn(Map.of("recorded", true));
        when(reviewService.getDueReviews("user-1")).thenReturn(List.of());

        controller.recordReview(Map.of("statementId", "statement-1", "quality", 4));

        InOrder order = inOrder(courseService, reviewService);
        order.verify(courseService).requireAccessibleStatement("statement-1");
        order.verify(reviewService).recordReview("user-1", "statement-1", 4);
    }

    @Test
    void dueReviews_shouldExcludeScheduledStatementsThatAreNoLongerAccessible() {
        Map<String, Object> visible = Map.of("statementId", "statement-public");
        Map<String, Object> hidden = Map.of("statementId", "statement-private");
        when(reviewService.getDueReviews("user-1")).thenReturn(List.of(visible, hidden));
        when(courseService.accessibleStatementIds(List.of("statement-public", "statement-private")))
                .thenReturn(Set.of("statement-public"));

        assertEquals(List.of(visible), controller.dueReviews());
    }

    @Test
    void dueReviewCount_shouldOnlyCountAccessibleScheduledStatements() {
        Map<String, Object> visible = Map.of("statementId", "statement-public");
        Map<String, Object> hidden = Map.of("statementId", "statement-private");
        when(reviewService.getDueReviews("user-1")).thenReturn(List.of(visible, hidden));
        when(courseService.accessibleStatementIds(List.of("statement-public", "statement-private")))
                .thenReturn(Set.of("statement-public"));

        assertEquals(Map.of("count", 1), controller.dueReviewCount());
    }

    @Test
    void getNotes_shouldValidateStatementAccessBeforeReading() {
        when(noteService.getNotes("statement-1")).thenReturn(List.of());

        controller.getNotes("statement-1");

        InOrder order = inOrder(courseService, noteService);
        order.verify(courseService).requireAccessibleStatement("statement-1");
        order.verify(noteService).getNotes("statement-1");
    }

    @Test
    void upsertNote_shouldValidateStatementAccessBeforeSaving() {
        when(noteService.upsertNote("statement-1", "note")).thenReturn(Map.of("saved", true));

        controller.upsertNote(Map.of("statementId", "statement-1", "content", "note"));

        InOrder order = inOrder(courseService, noteService);
        order.verify(courseService).requireAccessibleStatement("statement-1");
        order.verify(noteService).upsertNote("statement-1", "note");
    }

    @Test
    void addVocabulary_shouldValidateSuppliedStatementSourceBeforeSaving() {
        Map<String, Object> body = Map.of(
                "word", "word",
                "sourceStatementId", "statement-1",
                "sourceCoursePackId", "pack-1"
        );
        when(vocabularyService.addWord("user-1", "word", null, "statement-1", "pack-1", null))
                .thenReturn(Map.of("saved", true));

        controller.addVocabulary(body);

        InOrder order = inOrder(courseService, vocabularyService);
        order.verify(courseService).requireAccessibleStatementSource("statement-1", "pack-1");
        order.verify(vocabularyService).addWord("user-1", "word", null, "statement-1", "pack-1", null);
    }

    @Test
    void addVocabulary_shouldRejectCoursePackSourceWithoutStatementSource() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.addVocabulary(Map.of("word", "word", "sourceCoursePackId", "pack-1"))
        );

        verifyNoInteractions(vocabularyService);
    }

    @Test
    void dailyStats_shouldRejectInvalidDateWithoutQueryingData() {
        assertThrows(IllegalArgumentException.class, () -> controller.dailyStats("not-a-date", "2026-05-26"));

        verifyNoInteractions(statsService);
    }

    @Test
    void dailyStats_shouldRejectRangesLargerThanOneYear() {
        assertThrows(IllegalArgumentException.class, () -> controller.dailyStats("2024-01-01", "2026-05-26"));

        verifyNoInteractions(statsService);
    }

    @Test
    void dailyStats_shouldAllowOneYearRange() {
        LocalDate start = LocalDate.of(2025, 5, 25);
        LocalDate end = LocalDate.of(2026, 5, 26);
        when(statsService.getDailyStats("user-1", start, end)).thenReturn(List.of());

        assertEquals(List.of(), controller.dailyStats("2025-05-25", "2026-05-26"));

        verify(statsService).getDailyStats("user-1", start, end);
    }
}
