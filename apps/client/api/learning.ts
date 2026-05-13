import { getHttp } from "./http";

export interface DailyStat {
  date: string;
  totalExercises: number;
  correctExercises: number;
  totalTimeSeconds: number;
  maxCombo: number;
  totalScore: number;
  coursesCompleted: number;
}

export interface UserTotalStats {
  totalExercises: number;
  totalCorrect: number;
  totalTimeSeconds: number;
  totalScore: number;
  currentStreak: number;
  longestStreak: number;
  lastActiveDate: string | null;
}

export interface ExerciseRecord {
  id: string;
  correct: boolean;
  attempts: number;
  timeSpentMs: number;
  score: number;
}

export interface ReviewItem {
  id: string;
  statementId: string;
  coursePackId: string;
  courseId: string;
  easiness: number;
  interval: number;
  repetitions: number;
  nextReviewAt: string;
  lastReviewedAt: string | null;
}

export interface VocabularyItem {
  id: string;
  word: string;
  chinese: string | null;
  notes: string | null;
  createdAt: string;
}

export async function fetchSaveExercise(params: {
  coursePackId: string;
  courseId: string;
  statementId: string;
  correct: boolean;
  attempts: number;
  timeSpentMs: number;
  score: number;
  combo: number;
}) {
  const http = getHttp();
  return http<{ record: ExerciseRecord }>("/exercise-records", {
    method: "post",
    body: params,
  });
}

export async function fetchDailyStats(startDate?: string, endDate?: string) {
  const http = getHttp();
  return http<DailyStat[]>("/stats/daily", {
    method: "get",
    params: { startDate, endDate },
  });
}

export async function fetchTotalStats() {
  const http = getHttp();
  return http<UserTotalStats>("/stats/total", { method: "get" });
}

export async function fetchDueReviews() {
  const http = getHttp();
  return http<ReviewItem[]>("/reviews/due", { method: "get" });
}

export async function fetchDueReviewCount() {
  const http = getHttp();
  return http<{ count: number }>("/reviews/due-count", { method: "get" });
}

export async function fetchRecordReview(statementId: string, quality: number) {
  const http = getHttp();
  return http("/reviews/record", {
    method: "post",
    body: { statementId, quality },
  });
}

export async function fetchScheduleReview(statementId: string, coursePackId: string, courseId: string) {
  const http = getHttp();
  return http("/reviews/schedule", {
    method: "post",
    body: { statementId, coursePackId, courseId },
  });
}

export async function fetchVocabulary() {
  const http = getHttp();
  return http<VocabularyItem[]>("/vocabulary", { method: "get" });
}

export async function fetchAddVocabulary(params: {
  word: string;
  chinese?: string;
  sourceStatementId?: string;
  sourceCoursePackId?: string;
  notes?: string;
}) {
  const http = getHttp();
  return http<VocabularyItem>("/vocabulary", {
    method: "post",
    body: params,
  });
}

export async function fetchRemoveVocabulary(word: string) {
  const http = getHttp();
  return http<{ removed: boolean }>("/vocabulary", {
    method: "delete",
    params: { word },
  });
}
